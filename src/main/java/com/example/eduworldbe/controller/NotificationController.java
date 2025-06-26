package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.NotificationPageWithCursor;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.NotificationService;
import com.example.eduworldbe.util.AuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final AuthUtil authUtil;

  private String getCurrentUserId(HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);
    return currentUser.getId();
  }

  @GetMapping
  public ResponseEntity<?> getNotifications(
      HttpServletRequest request,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "5") int size) {
    String userId = getCurrentUserId(request);
    try {
      NotificationPageWithCursor notifications = notificationService.getNotificationsForUser(userId, cursor, size);
      return ResponseEntity.ok(notifications);
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("Error fetching notifications: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error fetching notifications: " + e.getMessage());
    }
  }

  @PutMapping("/{notificationId}/read")
  public ResponseEntity<?> markAsRead(HttpServletRequest request, @PathVariable String notificationId) {
    String userId = getCurrentUserId(request);
    try {
      notificationService.markNotificationAsRead(notificationId, userId);
      return ResponseEntity.ok().build();
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("Error marking notification as read: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error marking notification as read: " + e.getMessage());
    } catch (RuntimeException e) {
      System.err.println("Error marking notification as read: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
  }

  @PutMapping("/read-all")
  public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
    String userId = getCurrentUserId(request);
    try {
      notificationService.markAllNotificationsAsRead(userId);
      return ResponseEntity.ok().build();
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("Error marking all notifications as read: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error marking all notifications as read: " + e.getMessage());
    }
  }

  @DeleteMapping
  public ResponseEntity<?> deleteAllNotifications(HttpServletRequest request) {
    String userId = getCurrentUserId(request);
    try {
      notificationService.deleteAllNotificationsForUser(userId);
      return ResponseEntity.noContent().build();
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      System.err.println("Error deleting all notifications: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error deleting all notifications: " + e.getMessage());
    }
  }
}