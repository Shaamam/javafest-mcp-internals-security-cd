package tools.muthuishere.todo.todo;

import tools.muthuishere.todo.todo.model.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {
    
    // Email-based query methods for user-specific todos
    List<Todo> findByEmail(String email);
    
    Optional<Todo> findByIdAndEmail(Long id, String email);
    
    List<Todo> findByEmailAndCompleted(String email, boolean completed);
    
    long countByEmail(String email);
}
