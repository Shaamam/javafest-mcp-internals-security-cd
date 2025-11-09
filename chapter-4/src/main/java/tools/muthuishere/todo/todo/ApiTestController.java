package tools.muthuishere.todo.todo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.muthuishere.todo.todo.model.Todo;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiTestController {

    private final TodoService userTodoService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "service", "todo-mcp-server",
            "version", "1.0.0"
        ));
    }



    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUser() {
        try {
            String email = MCPContextHolder.getEmail();
            return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "email", email
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "authenticated", false,
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/user/todos")
    public ResponseEntity<?> getAllTodosForUser() {
        try {
            String email = MCPContextHolder.getEmail();
            var todos = userTodoService.getAllTodos(email);
            return ResponseEntity.ok(Map.of(
                "email", email,
                "count", todos.size(),
                "todos", todos
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "error", e.getMessage(),
                "todos", java.util.Collections.emptyList()
            ));
        }
    }

    @PostMapping("/user/todos")
    public ResponseEntity<?> createTodoForUser(@RequestBody Todo todo) {
        try {
            String email = MCPContextHolder.getEmail();
            Todo createdTodo = userTodoService.createTodo(todo, email);
            return ResponseEntity.ok(Map.of(
                "message", "Todo created successfully",
                "email", email,
                "todo", createdTodo
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
