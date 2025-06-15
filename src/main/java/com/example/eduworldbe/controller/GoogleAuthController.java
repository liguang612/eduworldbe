package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.response.GoogleAuthResponse;
import com.example.eduworldbe.service.GoogleAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {
  @Autowired
  private GoogleAuthService googleAuthService;

  @PostMapping("/login")
  public ResponseEntity<GoogleAuthResponse> loginWithGoogle(@RequestHeader("Authorization") String idToken) {
    try {
      GoogleAuthResponse response = googleAuthService.authenticateGoogleUser(idToken.replace("Bearer ", ""));
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}