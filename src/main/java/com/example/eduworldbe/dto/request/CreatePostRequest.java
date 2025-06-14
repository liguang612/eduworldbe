package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class CreatePostRequest {
  private String content;
  private List<String> imageUrls;
  private String courseId;
}