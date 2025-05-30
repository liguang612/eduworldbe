package com.example.eduworldbe.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {
  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(GoogleCredentials.fromStream(
            new ClassPathResource("service-account-file.json").getInputStream()))
        .setStorageBucket("eduworld-6ba8b.firebasestorage.app")
        .build();
    return FirebaseApp.initializeApp(options);
  }
}
