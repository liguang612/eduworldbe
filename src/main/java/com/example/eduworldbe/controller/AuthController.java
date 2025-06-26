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
import com.example.eduworldbe.service.LoginActivityService;
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

  @Autowired
  private LoginActivityService loginActivityService;

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
      @RequestParam(value = "avatar", required = false) MultipartFile avatar,
      @RequestParam(value = "googleAvatar", required = false) String googleAvatar) throws IOException {

    User user = new User();
    user.setEmail(email);
    user.setName(name);
    user.setSchool(school);
    user.setGrade(grade);
    user.setAddress(address);
    user.setRole(role != null ? role : 0);

    if (birthday != null) {
      try {
        user.setBirthday(java.sql.Date.valueOf(birthday));
      } catch (Exception e) {
        user.setBirthday(null);
      }
    }

    user.setPasswordHash(password);

    // Lưu user trước để có ID
    user = userService.register(user);

    if (avatar != null && !avatar.isEmpty()) {
      String avatarUrl = fileUploadService.uploadFile(avatar, "user", user.getId());
      user.setAvatar(avatarUrl);
      user = userService.update(user.getId(), user);
    } else if (googleAvatar != null && !googleAvatar.isEmpty()) {
      user.setAvatar(googleAvatar);
      user = userService.update(user.getId(), user);
    }

    return user;
  }

  @PostMapping("/login")
  public AuthResponse login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
    User user = userService.findByEmail(request.getEmail());
    if (user == null) {
      throw new RuntimeException("Email hoặc mật khẩu không chính xác");
    }
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new RuntimeException("Email hoặc mật khẩu không chính xác");
    }
    if (!user.getIsActive()) {
      throw new RuntimeException("Tài khoản đã bị vô hiệu hóa! Hãy liên hệ với quản trị viên để được hỗ trợ");
    }

    String token = jwtUtil.generateToken(user.getEmail());
    System.out.println("Login successful for user: " + user.getEmail() + " with token: " + token);

    try {
      loginActivityService.recordLoginActivity(user, "email", httpRequest);
    } catch (Exception e) {
      System.out.println("Error recording login activity: " + e.getMessage());
    }

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
        user.getBirthday() != null ? user.getBirthday().toString() : null,
        user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
        user.getIsActive(),
        null,
        user.getStorageLimit());
  }

  @PutMapping("/users")
  public UserResponse updateProfile(@RequestBody UpdateUserRequest request, HttpServletRequest httpRequest) {
    User currentUser = authUtil.requireActiveUser(httpRequest);

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
        savedUser.getBirthday() != null ? savedUser.getBirthday().toString() : null,
        savedUser.getCreatedAt() != null ? savedUser.getCreatedAt().toString() : null,
        savedUser.getIsActive(),
        null,
        savedUser.getStorageLimit());
  }

  @PutMapping("/users/{id}")
  public UserResponse updateProfile(@PathVariable String id, @RequestBody UpdateUserRequest request) {
    User currentUser = userService.findById(id);
    if (currentUser == null) {
      throw new RuntimeException("User not found");
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
        savedUser.getBirthday() != null ? savedUser.getBirthday().toString() : null,
        savedUser.getCreatedAt() != null ? savedUser.getCreatedAt().toString() : null,
        savedUser.getIsActive(),
        null,
        savedUser.getStorageLimit());
  }

  @PutMapping("/users/password")
  public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
    User currentUser = authUtil.requireActiveUser(httpRequest);

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
    User currentUser = authUtil.requireActiveUser(request);

    if (currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
      fileUploadService.deleteFile(currentUser.getAvatar());
    }

    String avatarUrl = fileUploadService.uploadFile(file, "user", currentUser.getId());
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
        savedUser.getBirthday() != null ? savedUser.getBirthday().toString() : null,
        savedUser.getCreatedAt() != null ? savedUser.getCreatedAt().toString() : null,
        savedUser.getIsActive(),
        null,
        savedUser.getStorageLimit()));
  }
}
