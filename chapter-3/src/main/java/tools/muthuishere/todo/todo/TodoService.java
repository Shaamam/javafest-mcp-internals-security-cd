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

    public List<Todo> getAllTodos(String email) {
        return todoRepository.findByEmail(email);
    }

    public Optional<Todo> getTodoById(Long id, String email) {
        return todoRepository.findByIdAndEmail(id, email);
    }

    public Todo createTodo(Todo todo, String email) {
        todo.setEmail(email);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        return todoRepository.save(todo);
    }

    public Optional<Todo> updateTodo(Long id, Todo todoDetails, String email) {
        return todoRepository.findByIdAndEmail(id, email).map(todo -> {
            todo.setTitle(todoDetails.getTitle());
            todo.setDescription(todoDetails.getDescription());
            todo.setCompleted(todoDetails.isCompleted());
            todo.setUpdatedAt(LocalDateTime.now());
            return todoRepository.save(todo);
        });
    }

    public boolean deleteTodo(Long id, String email) {
        return todoRepository.findByIdAndEmail(id, email).map(todo -> {
            todoRepository.delete(todo);
            return true;
        }).orElse(false);
    }

    public List<Todo> getCompletedTodos(String email) {
        return todoRepository.findByEmailAndCompleted(email, true);
    }

    public List<Todo> getPendingTodos(String email) {
        return todoRepository.findByEmailAndCompleted(email, false);
    }

    public long getTodoCount(String email) {
        return todoRepository.countByEmail(email);
    }

    public boolean markAsCompleted(Long id, String email) {
        return todoRepository.findByIdAndEmail(id, email).map(todo -> {
            todo.setCompleted(true);
            todo.setUpdatedAt(LocalDateTime.now());
            todoRepository.save(todo);
            return true;
        }).orElse(false);
    }

    public boolean markAsPending(Long id, String email) {
        return todoRepository.findByIdAndEmail(id, email).map(todo -> {
            todo.setCompleted(false);
            todo.setUpdatedAt(LocalDateTime.now());
            todoRepository.save(todo);
            return true;
        }).orElse(false);
    }
}