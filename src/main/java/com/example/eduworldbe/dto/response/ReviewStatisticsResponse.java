package com.example.eduworldbe.dto.response;

import lombok.Data;
import java.util.Map;

@Data
public class ReviewStatisticsResponse {
  private double averageScore;
  private Map<Integer, Long> scoreDistribution;
}