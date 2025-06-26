package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.response.GoogleAuthResponse;
import com.example.eduworldbe.service.GoogleAuthService;
import com.example.eduworldbe.service.LoginActivityService;
import com.example.eduworldbe.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth/google")
public class GoogleAuthController {
  @Autowired
  private GoogleAuthService googleAuthService;

  @Autowired
  private LoginActivityService loginActivityService;

  @PostMapping("/login")
  public ResponseEntity<GoogleAuthResponse> loginWithGoogle(@RequestHeader("Authorization") String idToken,
      HttpServletRequest request) {
    try {
      GoogleAuthResponse response = googleAuthService.authenticateGoogleUser(idToken.replace("Bearer ", ""));

      // Ghi lại hoạt động đăng nhập
      User user = new User();
      user.setId(response.getUserInfo().getId());
      user.setEmail(response.getUserInfo().getEmail());
      user.setRole(response.getUserInfo().getRole());

      if (!response.isNewUser()) {
        try {
          loginActivityService.recordLoginActivity(user, "google", request);
        } catch (Exception e) {
          System.out.println("Error recording login activity: " + e.getMessage());
        }
      }

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.badRequest().build();
    }
  }
}