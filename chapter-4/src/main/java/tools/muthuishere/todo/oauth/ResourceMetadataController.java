package tools.muthuishere.todo.oauth;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceMetadataController {

    @Value("${mcp.auth.server.base-url}")
    private String authServerBaseUrl;

    // @Value("${mcp.authorization.server.authorize-url}")
    // private String authorizeUrl;

    // @Value("${mcp.authorization.server.token-url}")
    // private String tokenUrl;

    /**
     * Constructs the base URL from request headers dynamically
     */
    private String constructBaseUrl(HttpServletRequest request) {
        // Determine protocol
        String protocol = "http";
        String xForwardedProto = request.getHeader("x-forwarded-proto");
        if (StringUtils.hasText(xForwardedProto)) {
            protocol = xForwardedProto;
        } else if (request.isSecure()) {
            protocol = "https";
        }

        // Get host
        String host = request.getHeader("host");
        if (!StringUtils.hasText(host)) {
            host = request.getServerName();
            int port = request.getServerPort();
            if (port != 80 && port != 443) {
                host += ":" + port;
            }
        }

        return protocol + "://" + host;
    }

    /**
     * OAuth 2.0 Protected Resource Metadata endpoint (without /mcp/)
     * Required for MCP Inspector discovery
     */
    @GetMapping(
            value = "/.well-known/oauth-protected-resource",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> getGenericResourceMetadata(HttpServletRequest request) {
        // Dynamically construct the MCP URL based on request headers
        String baseUrl = constructBaseUrl(request);
        String mcpUrl = baseUrl + "/mcp";

        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("resource_name", "Todo MCP Server");
        metadata.put("resource", mcpUrl);
        metadata.put(
                "authorization_servers",
                new String[] { authServerBaseUrl }
        );
        metadata.put("bearer_methods_supported", new String[] { "header" });
        metadata.put("scopes_supported", new String[] { "read:email" });
        // Return the same metadata as the MCP-specific endpoint
        return ResponseEntity.ok(metadata);
    }
}
