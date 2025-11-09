package tools.muthuishere.todo.todo;

import java.util.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


// works in any bean while on the request thread
public final class MCPContextHolder {
    private MCPContextHolder() {}

  public static String getEmail() {
        return findEmail().orElseThrow(() -> 
            new RuntimeException("No authenticated user found. Please authenticate with a valid Firebase JWT token."));
  }

    public static Optional<String> findEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt jwt) {
                return Optional.ofNullable(jwt.getClaimAsString("email"));
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
