package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.ChangeUserRoleRequest;
import com.example.eduworldbe.dto.request.UserSearchRequest;
import com.example.eduworldbe.dto.response.AdminDashboardResponse;
import com.example.eduworldbe.dto.response.LoginDetailResponse;
import com.example.eduworldbe.dto.response.StorageUsageResponse;
import com.example.eduworldbe.dto.response.UserListResponse;
import com.example.eduworldbe.dto.response.UserResponse;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.AdminService;
import com.example.eduworldbe.service.AdminUserService;
import com.example.eduworldbe.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  @Autowired
  private AdminService adminService;

  @Autowired
  private AdminUserService adminUserService;

  @Autowired
  private AuthUtil authUtil;

  @GetMapping("/dashboard")
  public ResponseEntity<AdminDashboardResponse> getDashboardData(HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    AdminDashboardResponse response = adminService.getDashboardData();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/storage-usage")
  public ResponseEntity<StorageUsageResponse> getStorageUsageData(HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    StorageUsageResponse response = adminService.getStorageUsageData();
    return ResponseEntity.ok(response);
  }

  // API quản lý user
  @PostMapping("/users")
  public ResponseEntity<UserListResponse> getAllUsers(@RequestBody UserSearchRequest request,
      HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    UserListResponse response = adminUserService.getAllUsers(request);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/users/{userId}/role")
  public ResponseEntity<UserResponse> changeUserRole(
      @PathVariable String userId,
      @RequestBody ChangeUserRoleRequest request,
      HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    User user = adminUserService.changeUserRole(userId, request.getRole());
    UserResponse response = convertToUserResponse(user);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/users/{userId}/status")
  public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable String userId, HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    User user = adminUserService.toggleUserStatus(userId);
    UserResponse response = convertToUserResponse(user);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/users/{userId}/reset-password")
  public ResponseEntity<String> resetUserPassword(@PathVariable String userId, HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    String newPassword = adminUserService.resetUserPassword(userId);
    return ResponseEntity.ok(newPassword);
  }

  @GetMapping("/users/new-month")
  public ResponseEntity<List<User>> getNewUsersToday(@RequestParam Integer month, @RequestParam Integer year,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }
    List<User> response = adminService.getNewUsersByMonth(month, year);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/logins/today")
  public ResponseEntity<List<LoginDetailResponse>> getDailyLoginsByDate(
      @RequestParam(required = false) String date,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    Date targetDate;
    if (date != null && !date.isEmpty()) {
      try {
        // Parse date từ string format "yyyy-MM-dd"
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        targetDate = sdf.parse(date);
      } catch (Exception e) {
        throw new RuntimeException("Invalid date format. Use yyyy-MM-dd");
      }
    } else {
      // Nếu không có date parameter, sử dụng hôm nay
      targetDate = new Date();
    }

    List<LoginDetailResponse> response = adminService.getDailyLoginsByDate(targetDate);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/logins/{id}")
  public ResponseEntity<LoginDetailResponse> getLoginDetailById(
      @PathVariable String id,
      HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    LoginDetailResponse response = adminService.getLoginDetailById(id);
    return ResponseEntity.ok(response);
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
        null,
        user.getStorageLimit());
  }
}