package dev.spiffocode.sigesapi.common.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }

    @Bean
    public FirebaseApp firebaseApp(FirebaseOptions firebaseOptions) {
        if(FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(firebaseOptions);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseOptions firebaseOptions(GoogleCredentials googleCredentials) {
        return FirebaseOptions.builder()
                .setCredentials(googleCredentials)
                .build();
    }

    @Bean
    public GoogleCredentials googleCredentials() {
        try {
            return GoogleCredentials.getApplicationDefault();
        } catch (IOException e) {
            log.warn("Could not load Google Credentials for Firebase APP. Push notifications might fail. Reason: {}",
                    e.getMessage());
        }
        return null;
    }
}
