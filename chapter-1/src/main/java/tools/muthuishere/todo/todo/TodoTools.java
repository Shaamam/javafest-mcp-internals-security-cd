package tools.muthuishere.todo.todo;

import jakarta.servlet.http.HttpServletRequest;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.muthuishere.todo.todo.model.Todo;
import tools.muthuishere.todo.todo.model.TodoToolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.context.request.RequestContextHolder;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class TodoTools {

    private final TodoService todoService;


    @McpTool(name = "fetch-all-todos", description = "Gets all Todo items")
    public List<Todo> fetchAllTodos() {

        Map<String,String> headers = HttpHeaderAccessor.all();
        System.out.println(headers);
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

        // Note: createSamplingRequest functionality needs to be updated without ToolContext
        String fact = "Todo created successfully!";

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
