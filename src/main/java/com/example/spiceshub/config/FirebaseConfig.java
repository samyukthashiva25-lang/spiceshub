package com.example.spiceshub.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        InputStream serviceAccount;

        // 1. Check if the environment variable exists (Cloud Environment)
        String firebaseConfigEnv = System.getenv("FIREBASE_CONFIG_JSON");

        if (firebaseConfigEnv != null && !firebaseConfigEnv.trim().isEmpty()) {
            // Read JSON credentials straight from the environment variable string
            serviceAccount = new ByteArrayInputStream(firebaseConfigEnv.getBytes(StandardCharsets.UTF_8));
        } else {
            // 2. Fallback to local file if the env variable isn't set (Local Development)
            serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream();
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        // Prevent double initialization during hot-reloads
        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }
}