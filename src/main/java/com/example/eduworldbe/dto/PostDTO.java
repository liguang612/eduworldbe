package com.example.eduworldbe.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDTO {
  private String id;
  private String content;
  private List<String> imageUrls;
  private LocalDateTime createdAt;
  private UserInfoDTO user;
  private String courseId;
  private boolean approved;
}
