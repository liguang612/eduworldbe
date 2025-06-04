package com.example.eduworldbe.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
  private String id;

  private String userId; // ID of the user who receives the notification

  private NotificationType type;

  private boolean isRead;

  private LocalDateTime createdAt;

  // ID of the user who performed the action that triggered the notification
  private String actorId;

  // Related entity IDs - all are Strings and can be null
  private String courseId;
  private String lectureId;
  private String questionId;
  private String solutionId;
  private String examId;
  private String postId;
  private String commentId;
  private String joinRequestId; // For join course request
}