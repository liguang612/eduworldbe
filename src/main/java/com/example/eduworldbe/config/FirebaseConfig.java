package com.example.eduworldbe.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

  @Value("${firebase.credentials}")
  private String firebaseCredentials;

  @Bean
  public FirebaseApp firebaseApp() throws IOException {
    if (FirebaseApp.getApps().isEmpty()) {
      byte[] decodedCredentials = Base64.getDecoder().decode(firebaseCredentials);

      GoogleCredentials credentials = GoogleCredentials.fromStream(
          new ByteArrayInputStream(decodedCredentials));

      FirebaseOptions options = FirebaseOptions.builder()
          .setCredentials(credentials)
          .build();

      return FirebaseApp.initializeApp(options);
    }
    return FirebaseApp.getInstance();
  }

  @Bean
  public FirebaseAuth firebaseAuth(FirebaseApp firebaseApp) {
    return FirebaseAuth.getInstance(firebaseApp);
  }
}
