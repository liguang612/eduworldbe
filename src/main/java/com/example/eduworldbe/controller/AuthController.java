package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.AuthRequest;
import com.example.eduworldbe.dto.AuthResponse;
import com.example.eduworldbe.dto.RegisterRequest;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.UserService;
import com.example.eduworldbe.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private JwtUtil jwtUtil;

  @PostMapping("/register")
  public User register(@RequestBody RegisterRequest request) {
    User user = new User();
    user.setEmail(request.getEmail());
    user.setName(request.getName());
    user.setAvatar(request.getAvatar());
    user.setSchool(request.getSchool());
    user.setGrade(request.getGrade());
    user.setAddress(request.getAddress());
    user.setRole(request.getRole() != null ? request.getRole() : 0); // mặc định là student
    if (request.getBirthday() != null) {
      try {
        user.setBirthday(java.sql.Date.valueOf(request.getBirthday()));
      } catch (Exception e) {
        user.setBirthday(null);
      }
    }
    user.setPasswordHash(request.getPassword());
    return userService.register(user);
  }

  @PostMapping("/login")
  public AuthResponse login(@RequestBody AuthRequest request) {
    User user = userService.findByEmail(request.getEmail());
    if (user == null) {
      throw new RuntimeException("Invalid email or password");
    }
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new RuntimeException("Invalid email or password");
    }
    String token = jwtUtil.generateToken(user.getEmail());
    System.out.println("Login successful for user: " + user.getEmail() + " with token: " + token);
    return new AuthResponse(token);
  }
}
