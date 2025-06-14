package com.example.eduworldbe.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class UpdatePostRequest {
  private String content;
  private List<String> imageUrls;
}