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
        String email = MCPContextHolder.getEmail();
        return todoService.getAllTodos(email);
    }

    @McpTool(name = "fetch-todo-by-id", description = "Gets a Todo item by ID")
    public Optional<Todo> fetchTodoById(
            @McpToolParam(description = "id for the Item")
            Long id
    ) {
        String email = MCPContextHolder.getEmail();
        return todoService.getTodoById(id, email);
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

        
        String email = MCPContextHolder.getEmail();
        
        Todo todo = Todo.builder()
                .title(title)
                .description(description)
                .completed(completed)
                .email(email)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Todo savedTodo = todoService.createTodo(todo, email);

        String fact = "Todo created successfully for user: " + email;

        return TodoToolResponse.builder()
                .todo(savedTodo)
                .fact(fact)
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
        String email = MCPContextHolder.getEmail();
        
        Todo todoDetails = Todo.builder()
                .title(title)
                .description(description)
                .completed(completed)
                .build();
                
        return todoService.updateTodo(id, todoDetails, email);
    }

    @McpTool(name = "remove-todo", description = "Deletes a Todo item by ID")
    public boolean removeTodo(
            @McpToolParam(description = "id for the Item")
            Long id
    ) {
        String email = MCPContextHolder.getEmail();
        return todoService.deleteTodo(id, email);
    }
}