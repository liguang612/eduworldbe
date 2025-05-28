package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.List;

@Data
public class PostPageResponse {
  private List<PostDTO> posts;
  private int currentPage;
  private int totalPages;
  private long totalElements;
  private int pageSize;
}