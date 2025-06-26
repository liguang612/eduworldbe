package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.response.NotificationResponse;
import com.example.eduworldbe.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {
  private final SimpMessagingTemplate messagingTemplate;
  private final NotificationService notificationService;

  public void sendNotificationToUser(String userId, Notification notification) {
    try {
      NotificationResponse response = notificationService.mapToResponse(notification);

      // Send to specific user
      messagingTemplate.convertAndSendToUser(
          userId,
          "/queue/notifications",
          response);

      log.info("Sent notification to user {}: {}", userId, notification.getId());
    } catch (Exception e) {
      log.error("Error sending notification to user {}: {}", userId, e.getMessage());
    }
  }
}