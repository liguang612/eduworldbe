package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.AuthRequest;
import com.example.eduworldbe.dto.AuthResponse;
import com.example.eduworldbe.dto.ChangePasswordRequest;
import com.example.eduworldbe.dto.RegisterRequest;
import com.example.eduworldbe.dto.UpdateUserRequest;
import com.example.eduworldbe.dto.UserResponse;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.UserService;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

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

  @Autowired
  private AuthUtil authUtil;

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
    return new AuthResponse(token, user.getId(), user.getName(), user.getAvatar());
  }

  @GetMapping("/users/{id}")
  public UserResponse getUserById(@PathVariable String id) {
    User user = userService.findById(id);
    if (user == null) {
      throw new RuntimeException("User not found");
    }
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getAvatar(),
        user.getSchool(),
        user.getGrade(),
        user.getAddress(),
        user.getRole(),
        user.getBirthday() != null ? user.getBirthday().toString() : null);
  }

  @PutMapping("/users")
  public UserResponse updateProfile(@RequestBody UpdateUserRequest request, HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    User updatedUser = new User();
    updatedUser.setName(request.getName());
    updatedUser.setAvatar(request.getAvatar());
    updatedUser.setSchool(request.getSchool());
    updatedUser.setGrade(request.getGrade());
    updatedUser.setAddress(request.getAddress());
    if (request.getBirthday() != null) {
      try {
        updatedUser.setBirthday(java.sql.Date.valueOf(request.getBirthday()));
      } catch (Exception e) {
        throw new RuntimeException("Invalid birthday format. Use yyyy-MM-dd");
      }
    }

    User savedUser = userService.update(currentUser.getId(), updatedUser);
    return new UserResponse(
        savedUser.getId(),
        savedUser.getEmail(),
        savedUser.getName(),
        savedUser.getAvatar(),
        savedUser.getSchool(),
        savedUser.getGrade(),
        savedUser.getAddress(),
        savedUser.getRole(),
        savedUser.getBirthday() != null ? savedUser.getBirthday().toString() : null);
  }

  @PutMapping("/users/password")
  public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    userService.changePassword(currentUser.getId(), request.getCurrentPassword(), request.getNewPassword());
    return ResponseEntity.ok().body("Password changed successfully");
  }
}
