package tools.muthuishere.todo.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Custom AuthenticationEntryPoint for MCP Authorization flow
 * Returns WWW-Authenticate header pointing to our Firebase Auth Proxy server
 * when JWT token is invalid/missing
 */
@Component
public class McpAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(McpAuthenticationEntryPoint.class);

    /**
     * Constructs the base URL from request headers and returns debug info
     * Handles different deployment scenarios:
     * - Local development (localhost)
     * - AWS Load Balancer (x-forwarded-proto, host)
     * - Google Cloud Run (x-forwarded-proto, host)
     * - Direct access (host header)
     */
    private Map<String, Object> constructBaseUrlWithDebug(HttpServletRequest request) {
        logger.info("=== Constructing Base URL ===");

        Map<String, Object> debugInfo = new HashMap<>();
        Map<String, String> headers = new HashMap<>();

        // Collect all request headers
        request.getHeaderNames().asIterator().forEachRemaining(headerName -> {
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
            logger.info("Header - {}: {}", headerName, headerValue);
        });
        debugInfo.put("requestHeaders", headers);

        // Determine protocol
        String protocol = "http";
        String xForwardedProto = request.getHeader("x-forwarded-proto");
        boolean isSecure = request.isSecure();

        logger.info("x-forwarded-proto header: {}", xForwardedProto);
        logger.info("request.isSecure(): {}", isSecure);

        String protocolSource;
        if (StringUtils.hasText(xForwardedProto)) {
            protocol = xForwardedProto;
            protocolSource = "x-forwarded-proto header";
            logger.info("Using x-forwarded-proto: {}", protocol);
        } else if (isSecure) {
            protocol = "https";
            protocolSource = "request.isSecure()";
            logger.info("Using secure request protocol: {}", protocol);
        } else {
            protocolSource = "default";
            logger.info("Using default protocol: {}", protocol);
        }

        debugInfo.put("protocolDetection", Map.of(
                "finalProtocol", protocol,
                "source", protocolSource,
                "xForwardedProto", xForwardedProto != null ? xForwardedProto : "null",
                "isSecure", isSecure
        ));

        // Get host
        String host = request.getHeader("host");
        String hostSource;
        logger.info("host header: {}", host);

        if (!StringUtils.hasText(host)) {
            host = request.getServerName();
            int port = request.getServerPort();
            hostSource = "server name + port";
            logger.info("Using server name: {}, port: {}", host, port);
            if (port != 80 && port != 443) {
                host += ":" + port;
                logger.info("Added port to host: {}", host);
            }
        } else {
            hostSource = "host header";
            logger.info("Using host header: {}", host);
        }

        debugInfo.put("hostDetection", Map.of(
                "finalHost", host,
                "source", hostSource,
                "hostHeader", request.getHeader("host") != null ? request.getHeader("host") : "null",
                "serverName", request.getServerName(),
                "serverPort", request.getServerPort()
        ));

        String baseUrl = protocol + "://" + host;
        logger.info("Final base URL: {}", baseUrl);
        logger.info("=== End Base URL Construction ===");

        debugInfo.put("finalBaseUrl", baseUrl);
        return debugInfo;
    }

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

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        String baseUrl = constructBaseUrl(request);

        String resourceMetadataUrl = baseUrl + "/.well-known/oauth-protected-resource";

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

    @SuppressWarnings("unchecked")
    private String toJsonString(Map<String, Object> map) {
        // Simple JSON serialization for debug info
        StringBuilder json = new StringBuilder("{");
        map.forEach((key, value) -> {
            if (json.length() > 1) json.append(",");
            json.append("\"").append(key).append("\":");
            if (value instanceof Map) {
                json.append(toJsonString((Map<String, Object>) value));
            } else if (value instanceof String) {
                json.append("\"").append(value.toString().replace("\"", "\\\"")).append("\"");
            } else {
                json.append("\"").append(value).append("\"");
            }
        });
        json.append("}");
        return json.toString();
    }
}