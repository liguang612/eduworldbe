package com.example.eduworldbe.dto.response;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ReviewPageResponse {
  private List<ReviewResponse> reviews;
  private int currentPage;
  private int totalPages;
  private long totalElements;
  private double averageScore;
  private Map<Integer, Long> scoreDistribution;
}