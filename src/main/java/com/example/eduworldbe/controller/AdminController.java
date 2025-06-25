package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.ChangeUserRoleRequest;
import com.example.eduworldbe.dto.request.UserSearchRequest;
import com.example.eduworldbe.dto.response.AdminDashboardResponse;
import com.example.eduworldbe.dto.response.StorageUsageResponse;
import com.example.eduworldbe.dto.response.UserListResponse;
import com.example.eduworldbe.dto.response.UserResponse;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.AdminService;
import com.example.eduworldbe.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @Autowired
  private AdminService adminService;

  @Autowired
  private AdminUserService adminUserService;

  @GetMapping("/dashboard")
  public ResponseEntity<AdminDashboardResponse> getDashboardData() {
    AdminDashboardResponse response = adminService.getDashboardData();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/storage-usage")
  public ResponseEntity<StorageUsageResponse> getStorageUsageData() {
    StorageUsageResponse response = adminService.getStorageUsageData();
    return ResponseEntity.ok(response);
  }

  // API quản lý user
  @PostMapping("/users")
  public ResponseEntity<UserListResponse> getAllUsers(@RequestBody UserSearchRequest request) {
    UserListResponse response = adminUserService.getAllUsers(request);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/users/{userId}/role")
  public ResponseEntity<UserResponse> changeUserRole(
      @PathVariable String userId,
      @RequestBody ChangeUserRoleRequest request) {
    User user = adminUserService.changeUserRole(userId, request.getRole());
    UserResponse response = convertToUserResponse(user);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/users/{userId}/status")
  public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable String userId) {
    User user = adminUserService.toggleUserStatus(userId);
    UserResponse response = convertToUserResponse(user);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/users/{userId}/reset-password")
  public ResponseEntity<String> resetUserPassword(@PathVariable String userId) {
    String newPassword = adminUserService.resetUserPassword(userId);
    return ResponseEntity.ok(newPassword);
  }

  private UserResponse convertToUserResponse(User user) {
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
        null);
  }
}