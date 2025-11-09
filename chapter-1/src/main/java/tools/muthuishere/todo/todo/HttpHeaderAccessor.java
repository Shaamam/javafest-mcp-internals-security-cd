package tools.muthuishere.todo.todo;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.*;

import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


// works in any bean while on the request thread
public final class HttpHeaderAccessor {
    private HttpHeaderAccessor() {}

    public static Optional<String> get(String name) {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Optional.empty();
        var req = attrs.getRequest();
        return Optional.ofNullable(req.getHeader(name));
    }

    public static Map<String,String> all() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Map.of();
        var req = attrs.getRequest();
        var map = new LinkedHashMap<String,String>();
        var names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            var n = names.nextElement();
            map.put(n, req.getHeader(n));
        }
        return Map.copyOf(map);
    }
}
