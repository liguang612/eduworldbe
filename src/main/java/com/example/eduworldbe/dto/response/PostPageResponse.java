package com.example.eduworldbe.dto.response;

import lombok.Data;
import java.util.List;

import com.example.eduworldbe.dto.PostDTO;

@Data
public class PostPageResponse {
  private List<PostDTO> posts;
  private int currentPage;
  private int totalPages;
  private long totalElements;
  private int pageSize;
}