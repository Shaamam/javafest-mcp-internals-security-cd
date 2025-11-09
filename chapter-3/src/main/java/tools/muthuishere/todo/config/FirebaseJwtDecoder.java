package tools.muthuishere.todo.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom JWT decoder for Firebase JWT tokens.
 * This validates Firebase ID tokens by checking the issuer and basic claims.
 * Also handles OAuth2 access tokens from our auth server by extracting Firebase tokens.
 */
public class FirebaseJwtDecoder implements JwtDecoder {

//    @Value("${mcp.authorization.server.url}")
//    private String authServerUrl;

    @Override
    public Jwt decode(String token) throws JwtException {

//        System.out.println("Decoding token: " + token);
        // Extract Firebase token from JWT claims
        String firebaseToken = extractFirebaseToken(token);
        
        // Use the Firebase token for validation
        try {
            // Use Firebase Admin SDK to verify the token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);
            Map<String, Object> claims = new HashMap<>();
            decodedToken.getClaims().forEach((key,obj)->{
                claims.put(key, obj.toString());
            });

            // Add email if present
            if (decodedToken.getEmail() != null) {
                claims.put("email", decodedToken.getEmail());
                claims.put("email_verified", decodedToken.isEmailVerified());
            }

            // Add name if present
            if (decodedToken.getName() != null) {
                claims.put("name", decodedToken.getName());
            }

            // Add picture if present
            if (decodedToken.getPicture() != null) {
                claims.put("picture", decodedToken.getPicture());
            }

            // Add custom claims
            claims.put("firebase", decodedToken.getClaims());

            // Create header map
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "RS256");
            headers.put("typ", "JWT");


            // Claims map contains standard JWT fields too
            long iat = ((Number) decodedToken.getClaims().get("iat")).longValue(); // issued-at (seconds)
            long exp = ((Number) decodedToken.getClaims().get("exp")).longValue(); // expires-at (seconds)

// Convert to Java Instant / ZonedDateTime
            Instant issuedAt = Instant.ofEpochSecond(iat);
            Instant expiresAt = Instant.ofEpochSecond(exp);


            return new Jwt(
                firebaseToken, // Use the Firebase token as the token value
                issuedAt,
                expiresAt,
                headers,
                claims
            );

        } catch (FirebaseAuthException e) {
            throw new JwtException("Firebase token validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new JwtException("Token processing error: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts Firebase token from JWT claims using Nimbus JWT.
     * No verification, just decode and extract firebase_token claim.
     */
    private String extractFirebaseToken(String token) {
        try {
            // Parse JWT using Nimbus (no verification)
            JWT jwt = JWTParser.parse(token);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            
            // Extract firebase_token claim and return it
            String firebaseToken = claims.getStringClaim("firebase_token");
            if (firebaseToken != null && !firebaseToken.isEmpty()) {
                System.out.println("Extracted Firebase token from JWT claims");
                return firebaseToken;
            }
            
        } catch (Exception e) {
            System.out.println("JWT parsing failed: " + e.getMessage());
        }
        
        // If extraction fails or no firebase_token claim, return original token
        return token;
    }


}