# Building a Multi-Protocol MCP Server with Spring Boot

This guide shows how to create a Spring Boot Todo application that can serve as different types of Model Context Protocol (MCP) servers.

## Step 1: Project Setup

Add repositories to `build.gradle`:
```gradle
repositories {
    mavenCentral()
    maven { url 'https://repo.spring.io/milestone' }
    maven { url 'https://repo.spring.io/snapshot' }
    maven {
        name = 'Central Portal Snapshots'
        url = 'https://central.sonatype.com/repository/maven-snapshots/'
    }
}
```

Add dependencies:
```gradle
dependencies {
    implementation platform("org.springframework.ai:spring-ai-bom:1.1.0-M3")
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    runtimeOnly 'com.h2database:h2'
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    implementation 'org.springframework.ai:spring-ai-starter-mcp-server-webmvc'
    implementation 'org.springframework.ai:spring-ai-starter-mcp-server'
    implementation 'org.springframework.ai:spring-ai-mcp-annotations'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

## Step 2: Configure Profiles

### Main Configuration (`application.properties`)
```properties
spring.application.name=todoapp
spring.profiles.active=stdio

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:todo-db
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# Enable H2 Console
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### STDIO Profile (`application-stdio.properties`)
```properties
# STDIO Profile - Standard Input/Output communication
# Uses spring-ai-starter-mcp-server dependency

# Disable web server for STDIO (no HTTP server needed)
spring.main.web-application-type=none

# Spring AI MCP Server STDIO configuration
spring.ai.mcp.server.stdio=true

# Disable Spring Boot banner
spring.main.banner-mode=off

###################################################################################
# Logging Configuration
###################################################################################

# Disable console logging completely for STDIO communication
logging.threshold.console=OFF

# Only use file logging
logging.file.name=${user.home}/mcp-server-stdio.log
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.max-history=10
# Set logging levels
logging.level.root=INFO
logging.level.org.springframework=WARN
logging.level.com.bothub.movie_mcp_server=DEBUG
# File log pattern
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### SSE Profile (`application-sse.properties`)
```properties
# SSE WebMVC Profile - Server-Sent Events
# Uses spring-ai-starter-mcp-server-webmvc dependency
spring.ai.mcp.server.stdio=false
# Spring AI MCP Server SSE configuration
spring.ai.mcp.server.protocol=SSE

# Server configuration for WebMVC
server.port=8080

# Note: MCP server metadata (name, version, description) are defined in main application.properties
```

### Streamable Profile (`application-streamable.properties`)
```properties
# Streamable WebMVC Profile - Streamable HTTP
# Uses spring-ai-starter-mcp-server-webmvc dependency

# Spring AI MCP Server Streamable configuration
spring.ai.mcp.server.protocol=STREAMABLE

# Server configuration for WebMVC
server.port=8080

logging.level.root=INFO
logging.level.org.apache.tomcat.util.compat=ERROR
# Note: MCP server metadata (name, version, description) are defined in main application.properties
```

## Step 3: MCP Server Types

One application can act as:
- **STDIO Server**: Standard input/output communication
- **SSE Server**: Server-Sent Events over HTTP  
- **Streamable HTTP Server**: HTTP POST/GET with streaming http

Switch between types by changing the active profile.

## Step 4: Create Todo Domain Model

The Todo entity represents our main data structure with JPA annotations for database persistence.

### Todo Entity (`Todo.java`)
```java
package tools.muthuishere.todo.todo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private boolean completed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
```

**Key Points:**
- `@Entity`: Marks this as a JPA entity for database persistence
- `@Data`: Lombok annotation that generates getters, setters, toString, equals, and hashCode
- `@Builder`: Lombok annotation that provides a builder pattern for object creation
- `@NotBlank`: Validation annotation ensuring title is not empty
- Auto-generated ID with `@GeneratedValue`

### Todo Response Model (`TodoToolResponse.java`)
```java
package tools.muthuishere.todo.todo.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodoToolResponse {
    private Todo todo;
    private String fact;
}
```

**Purpose:** This wrapper class is used by MCP tools to return both the Todo object and additional context information to the client.

## Step 5: Create Repository Layer

The repository provides data access operations using Spring Data JPA.

### Todo Repository (`TodoRepository.java`)
```java
package tools.muthuishere.todo.todo;

import tools.muthuishere.todo.todo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
}
```

**Key Points:**
- Extends `JpaRepository<Todo, Long>` providing CRUD operations automatically
- Spring Data JPA generates implementation at runtime
- No custom queries needed for basic operations
- `Long` represents the ID type of the Todo entity

## Step 6: Create Service Layer

The service layer contains business logic and acts as a bridge between the repository and MCP tools.

### Todo Service (`TodoService.java`)
```java
package tools.muthuishere.todo.todo;

import tools.muthuishere.todo.todo.model.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }

    public Todo createTodo(Todo todo) {
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        return todoRepository.save(todo);
    }

    public Optional<Todo> updateTodo(Long id, Todo todoDetails) {
        return todoRepository.findById(id).map(todo -> {
            todo.setTitle(todoDetails.getTitle());
            todo.setDescription(todoDetails.getDescription());
            todo.setCompleted(todoDetails.isCompleted());
            todo.setUpdatedAt(LocalDateTime.now());
            return todoRepository.save(todo);
        });
    }

    public boolean deleteTodo(Long id) {
        return todoRepository.findById(id).map(todo -> {
            todoRepository.delete(todo);
            return true;
        }).orElse(false);
    }
}
```

**Key Points:**
- `@Service`: Marks this as a Spring service component
- `@RequiredArgsConstructor`: Lombok generates constructor for final fields (dependency injection)
- **getAllTodos()**: Retrieves all todos from database
- **getTodoById()**: Returns Optional<Todo> - handles case when todo doesn't exist
- **createTodo()**: Sets timestamps and saves new todo
- **updateTodo()**: Uses Optional.map() for safe updates, returns empty if todo not found
- **deleteTodo()**: Returns boolean indicating success/failure

## Step 7: Expose as MCP Tools

Instead of traditional REST controllers, we use `@McpTool` annotations to expose functionality as MCP tools that AI clients can discover and use.

### Todo Tools (`TodoTools.java`)
```java
package tools.muthuishere.todo.todo;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import tools.muthuishere.todo.todo.model.Todo;
import tools.muthuishere.todo.todo.model.TodoToolResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TodoTools {

    private final TodoService todoService;

    @McpTool(name = "fetch-all-todos", description = "Gets all Todo items")
    public List<Todo> fetchAllTodos() {
        return todoService.getAllTodos();
    }

    @McpTool(name = "fetch-todo-by-id", description = "Gets a Todo item by ID")
    public Optional<Todo> fetchTodoById(
            @McpToolParam(description = "id for the Item")
            Long id
    ) {
        return todoService.getTodoById(id);
    }

    @McpTool(name = "make-todo", description = "Creates a new Todo item")
    public TodoToolResponse makeTodo(
            @McpToolParam(description = "Title for the Todo")
            String title,

            @McpToolParam(description = "Description for the Todo")
            String description,

            @McpToolParam(description = "Is the Todo completed?")
            boolean completed
    ) {
        Todo todo = Todo.builder()
                .title(title)
                .description(description)
                .completed(completed)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Todo savedTodo = todoService.createTodo(todo);

        return TodoToolResponse.builder()
                .todo(savedTodo)
                .fact("Todo created successfully!")
                .build();
    }

    @McpTool(name = "change-todo", description = "Updates an existing Todo item")
    public Optional<Todo> changeTodo(
            @McpToolParam(description = "id for the Item")
            Long id,

            @McpToolParam(description = "Title for the Todo")
            String title,

            @McpToolParam(description = "Description for the Todo")
            String description,

            @McpToolParam(description = "Is the Todo completed?")
            boolean completed
    ) {
        return todoService.getTodoById(id).map(todo -> {
            todo.setTitle(title);
            todo.setDescription(description);
            todo.setCompleted(completed);
            todo.setUpdatedAt(LocalDateTime.now());
            return todoService.createTodo(todo);
        });
    }

    @McpTool(name = "remove-todo", description = "Deletes a Todo item by ID")
    public boolean removeTodo(
            @McpToolParam(description = "id for the Item")
            Long id
    ) {
        return todoService.getTodoById(id).map(todo -> {
            todoService.deleteTodo(id);
            return true;
        }).orElse(false);
    }
}
```

**Key Points:**
- `@Component`: Marks this as a Spring component (not @Service since it's not business logic)
- `@McpTool`: Exposes method as an MCP tool with name and description
- `@McpToolParam`: Describes parameters for AI clients to understand
- **fetch-all-todos**: Returns all todos - simple delegation to service
- **fetch-todo-by-id**: Uses Optional to handle missing todos gracefully
- **make-todo**: Uses builder pattern, returns TodoToolResponse with additional context
- **change-todo**: Updates existing todo, returns Optional for safe handling
- **remove-todo**: Returns boolean to indicate success/failure
- Spring automatically discovers these tools and registers them with the MCP server

## Step 6: Build and Run Tasks

Create `Taskfile.yaml`:
```yaml
version: '3'

tasks:
  build:stdio:
    desc: "Build JAR for STDIO MCP server"
    cmds:
      - ./gradlew clean bootJar
      
  dev:sse:
    desc: "Run application in development mode with SSE profile"
    cmds:
      - ./gradlew bootRun --args='--spring.profiles.active=sse'
    interactive: true
    
  dev:streamable:
    desc: "Run application in development mode with Streamable profile"
    cmds:
      - ./gradlew bootRun --args='--spring.profiles.active=streamable'
    interactive: true
```

## Step 7: MCP Client Configuration

### STDIO Server
```json
"todo-mcp-server-stdio": {
    "type": "stdio",
    "command": "java",
    "args": [
        "-Dspring.profiles.active=stdio",
        "-jar",
        "/path/to/build/libs/todo-0.0.1-SNAPSHOT.jar"
    ]
}
```

### SSE Server  
```json
"todo-mcp-server-sse": {
    "url": "http://localhost:8080/sse",
    "type": "sse"
}
```

### Streamable HTTP Server
```json
"todo-mcp-server-streamable": {
    "url": "http://localhost:8080/mcp",
    "type": "http"
}
```

### Stateless Streamable HTTP Server
```json
"todo-mcp-server-stateless": {
    "url": "http://localhost:8080/mcp",
    "type": "http"
}
```

## Available MCP Tools

- `fetch-all-todos`: Get all todo items
- `fetch-todo-by-id`: Get a specific todo by ID  
- `make-todo`: Create a new todo item
- `change-todo`: Update an existing todo
- `remove-todo`: Delete a todo by ID