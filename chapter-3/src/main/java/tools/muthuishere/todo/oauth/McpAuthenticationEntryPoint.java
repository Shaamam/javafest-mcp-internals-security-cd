package tools.muthuishere.todo.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom AuthenticationEntryPoint for MCP Authorization flow
 * Returns WWW-Authenticate header pointing to our Firebase Auth Proxy server
 * when JWT token is invalid/missing
 */
@Component
public class McpAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Value("${mcp.server.base-url}")
    private String mcpServerBaseUrl;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        String resourceMetadataUrl =
                mcpServerBaseUrl + "/.well-known/oauth-protected-resource";

        // Set WWW-Authenticate header as per RFC 6750 and RFC 8707
        response.setHeader(
                "WWW-Authenticate",
                "Bearer error=\"invalid_request\", " +
                        "error_description=\"No access token was provided in this request\", " +
                        "resource_metadata=\"" +
                        resourceMetadataUrl +
                        "\""
        );

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        String jsonResponse = """
            {
                "error": "invalid_request",
                "error_description": "No access token was provided in this request",
                "resource_metadata": "%s"
            }
            """.formatted(resourceMetadataUrl);

        response.getWriter().write(jsonResponse);
    }
}