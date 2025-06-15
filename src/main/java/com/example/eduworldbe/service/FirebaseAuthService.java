package com.example.eduworldbe.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

  private final FirebaseAuth firebaseAuth;

  public FirebaseAuthService(FirebaseAuth firebaseAuth) {
    this.firebaseAuth = firebaseAuth;
  }

  public FirebaseToken verifyToken(String idToken) throws Exception {
    return firebaseAuth.verifyIdToken(idToken);
  }

  public String getUidFromToken(String idToken) throws Exception {
    FirebaseToken decodedToken = verifyToken(idToken);
    return decodedToken.getUid();
  }
}