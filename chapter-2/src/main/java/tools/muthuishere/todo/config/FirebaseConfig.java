package tools.muthuishere.todo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-key:}")
    private String serviceAccountKey;

    @Value("${firebase.project-id:}")
    private String projectId;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder();
                
                if (!serviceAccountKey.isEmpty()) {
                    // Use service account key if provided
                    InputStream serviceAccount = new ByteArrayInputStream(serviceAccountKey.getBytes());
                    GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                    optionsBuilder.setCredentials(credentials);
                } else {
                    // Use default credentials (works in Google Cloud environments)
                    optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault());
                }
                

                    optionsBuilder.setProjectId(projectId);

                
                FirebaseApp.initializeApp(optionsBuilder.build());
                System.out.println("Firebase Admin SDK initialized successfully");
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase Admin SDK: " + e.getMessage());
           throw new RuntimeException(e);
        }
    }
}