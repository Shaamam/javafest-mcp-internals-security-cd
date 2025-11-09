package tools.muthuishere.todo.oauth;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @Value("${mcp.server.mcp-url}")
    private String mcpUrl;

    /**
     * OAuth 2.0 Protected Resource Metadata endpoint (without /mcp/)
     * Required for MCP Inspector discovery
     */
    @GetMapping(
            value = "/.well-known/oauth-protected-resource",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Map<String, Object>> getGenericResourceMetadata() {
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