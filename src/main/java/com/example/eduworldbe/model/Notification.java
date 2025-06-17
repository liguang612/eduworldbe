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

  private String userId;

  private NotificationType type;

  private boolean isRead;

  private LocalDateTime createdAt;

  private String actorId;

  private String courseId;
  private String lectureId;
  private String questionId;
  private String solutionId;
  private String examId;
  private String postId;
  private String commentId;
  private String joinRequestId;
}