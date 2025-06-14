package com.example.eduworldbe.dto.response;

import lombok.Data;
import java.util.List;

import com.example.eduworldbe.dto.CommentDTO;

@Data
public class CommentPageResponse {
  private List<CommentDTO> comments;
  private int currentPage;
  private int totalPages;
  private long totalElements;
  private int pageSize;
}