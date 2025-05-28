package com.example.eduworldbe.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDTO {
  private String id;
  private String postId;
  private UserInfoDTO user;
  private LocalDateTime createdAt;
  private String content;
}