package com.example.eduworldbe.dto.response;

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
  private String message;
  private NotificationType type;
  private boolean isRead;
  private LocalDateTime createdAt;

  // Actor (người có sự kiện gây ra thông báo)
  private String actorId;
  private String actorName;
  private String actorAvatarUrl;

  private String courseId;
  private String courseName;
  private String courseAvatarUrl;

  private String lectureId;
  private String lectureTitle;

  private String questionId;
  private String questionTitle;

  private String solutionId;

  private String examId;
  private String examTitle;

  private String postId;
  private String postTitle;

  private String commentId;
  private String commentContentSnippet;

  private String joinRequestId;
}