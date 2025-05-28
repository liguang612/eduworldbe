package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.List;

@Data
public class CommentPageResponse {
  private List<CommentDTO> comments;
  private int currentPage;
  private int totalPages;
  private long totalElements;
  private int pageSize;
}