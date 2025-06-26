package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.TeacherSearchRequest;
import com.example.eduworldbe.dto.request.UpdateStorageLimitRequest;
import com.example.eduworldbe.dto.response.TeacherStorageListResponse;
import com.example.eduworldbe.dto.response.UserFileDetailResponse;
import com.example.eduworldbe.dto.response.UserStorageInfoResponse;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.StorageUsageService;
import com.example.eduworldbe.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/storage-usage")
@CrossOrigin(origins = "*")
public class StorageUsageController {

  @Autowired
  private StorageUsageService storageUsageService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping("/teachers")
  public ResponseEntity<TeacherStorageListResponse> getTeacherStorageList(@RequestBody TeacherSearchRequest request) {
    TeacherStorageListResponse teachers = storageUsageService.getTeacherStorageList(request);
    return ResponseEntity.ok(teachers);
  }

  @GetMapping("/users/{userId}/files")
  public ResponseEntity<List<UserFileDetailResponse>> getUserFileDetails(@PathVariable String userId) {
    List<UserFileDetailResponse> files = storageUsageService.getUserFileDetails(userId);
    return ResponseEntity.ok(files);
  }

  @GetMapping("/users/{userId}/total")
  public ResponseEntity<Long> getUserTotalStorage(@PathVariable String userId) {
    Long totalStorage = storageUsageService.getTotalStorageUsedByUser(userId);
    return ResponseEntity.ok(totalStorage);
  }

  @GetMapping("/total")
  public ResponseEntity<Long> getTotalStorage() {
    Long totalStorage = storageUsageService.getTotalStorageUsed();
    return ResponseEntity.ok(totalStorage);
  }

  @PutMapping("/admin/users/{userId}/storage-limit")
  public ResponseEntity<UserStorageInfoResponse> updateUserStorageLimit(
      @PathVariable String userId,
      @RequestBody UpdateStorageLimitRequest request,
      HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null || currentUser.getRole() != 100) {
      throw new RuntimeException("Unauthorized");
    }

    UserStorageInfoResponse updatedInfo = storageUsageService.updateUserStorageLimit(userId, request.getStorageLimit());
    return ResponseEntity.ok(updatedInfo);
  }
}