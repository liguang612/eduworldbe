package com.example.eduworldbe.util;

import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  public User getCurrentUser(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      String email = jwtUtil.extractEmail(token);
      return userRepository.findByEmail(email).orElse(null);
    }
    return null;
  }
}
