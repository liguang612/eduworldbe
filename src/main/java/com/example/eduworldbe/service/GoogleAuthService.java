package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.response.GoogleAuthResponse;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.util.JwtUtil;
import com.google.firebase.auth.FirebaseToken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoogleAuthService {
  @Autowired
  private FirebaseAuthService firebaseAuthService;

  @Autowired
  private UserService userService;

  @Autowired
  private JwtUtil jwtUtil;

  @Transactional
  public GoogleAuthResponse authenticateGoogleUser(String idToken) throws Exception {
    FirebaseToken decodedToken = firebaseAuthService.verifyToken(idToken);

    boolean isNewUser = !userService.existsByEmail(decodedToken.getEmail());

    User user;
    if (isNewUser) {
      user = userService.createUserFromGoogle(decodedToken);
    } else {
      user = userService.findByEmail(decodedToken.getEmail());
    }

    String accessToken = jwtUtil.generateToken(user.getEmail());
    // String refreshToken = jwtService.generateRefreshToken(user);

    return GoogleAuthResponse.builder()
        .accessToken(accessToken)
        // .refreshToken(refreshToken)
        .isNewUser(isNewUser)
        .userInfo(GoogleAuthResponse.UserInfo.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getName())
            .avatar(user.getAvatar())
            .role(user.getRole())
            .build())
        .build();
  }
}