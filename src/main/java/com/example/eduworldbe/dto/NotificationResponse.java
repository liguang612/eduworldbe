package com.example.eduworldbe.dto;

import com.example.eduworldbe.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
  private String id;
  private String message; // Human-readable message
  private NotificationType type;
  private boolean isRead;
  private LocalDateTime createdAt;

  // Actor (user who triggered the notification)
  private String actorId;
  private String actorName; // e.g., "John Doe commented..."
  private String actorAvatarUrl; // Optional: URL for actor's avatar

  // Related entity information
  private String courseId;
  private String courseName;
  private String courseAvatarUrl; // Optional: URL for course avatar

  private String lectureId;
  private String lectureTitle;

  private String questionId;
  private String questionTitle; // Or a snippet of question content

  private String solutionId;

  private String examId;
  private String examTitle;

  private String postId;
  private String postTitle; // Or a snippet of post content

  private String commentId;
  private String commentContentSnippet; // Snippet of the comment

  private String joinRequestId;
}