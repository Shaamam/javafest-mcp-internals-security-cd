package tools.muthuishere.todo.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom JWT decoder for Firebase JWT tokens.
 * This validates Firebase ID tokens by checking the issuer and basic claims.
 * In production, you should verify the signature with Firebase's public keys.
 */
public class FirebaseJwtDecoder implements JwtDecoder {

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // Use Firebase Admin SDK to verify the token
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
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
                token,
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
}