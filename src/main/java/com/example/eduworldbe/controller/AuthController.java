package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.AuthRequest;
import com.example.eduworldbe.dto.request.ChangePasswordRequest;
import com.example.eduworldbe.dto.request.UpdateUserRequest;
import com.example.eduworldbe.dto.response.AuthResponse;
import com.example.eduworldbe.dto.response.UserResponse;
import com.example.eduworldbe.dto.response.UserSearchResponse;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.UserService;
import com.example.eduworldbe.service.FileUploadService;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

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

  @Autowired
  private FileUploadService fileUploadService;

  @PostMapping("/register")
  public User register(
      @RequestParam(value = "email") String email,
      @RequestParam(value = "password") String password,
      @RequestParam(value = "name") String name,
      @RequestParam(value = "school", required = false) String school,
      @RequestParam(value = "grade", required = false) Integer grade,
      @RequestParam(value = "address", required = false) String address,
      @RequestParam(value = "role", required = false) Integer role,
      @RequestParam(value = "birthday", required = false) String birthday,
      @RequestParam(value = "avatar", required = false) MultipartFile avatar) throws IOException {

    User user = new User();
    user.setEmail(email);
    user.setName(name);
    user.setSchool(school);
    user.setGrade(grade);
    user.setAddress(address);
    user.setRole(role != null ? role : 0); // mặc định là student

    if (birthday != null) {
      try {
        user.setBirthday(java.sql.Date.valueOf(birthday));
      } catch (Exception e) {
        user.setBirthday(null);
      }
    }

    user.setPasswordHash(password);

    // Upload avatar if provided
    if (avatar != null && !avatar.isEmpty()) {
      String avatarUrl = fileUploadService.uploadFile(avatar, "user");
      user.setAvatar(avatarUrl);
    }

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
    return new AuthResponse(token, user.getId(), user.getName(), user.getAvatar(), user.getRole());
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

  @GetMapping("/users/search")
  public List<UserSearchResponse> searchUsers(
      @RequestParam String email,
      @RequestParam Integer role) {
    return userService.searchUsers(email, role);
  }

  @PostMapping("/users/avatar")
  public ResponseEntity<UserResponse> uploadAvatar(
      @RequestParam("file") MultipartFile file,
      HttpServletRequest request) throws IOException {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    // Delete old avatar if exists
    if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
      fileUploadService.deleteFile(currentUser.getAvatar());
    }

    String avatarUrl = fileUploadService.uploadFile(file, "user");
    User updatedUser = new User();
    updatedUser.setAvatar(avatarUrl);

    User savedUser = userService.update(currentUser.getId(), updatedUser);

    return ResponseEntity.ok(new UserResponse(
        savedUser.getId(),
        savedUser.getEmail(),
        savedUser.getName(),
        savedUser.getAvatar(),
        savedUser.getSchool(),
        savedUser.getGrade(),
        savedUser.getAddress(),
        savedUser.getRole(),
        savedUser.getBirthday() != null ? savedUser.getBirthday().toString() : null));
  }
}
