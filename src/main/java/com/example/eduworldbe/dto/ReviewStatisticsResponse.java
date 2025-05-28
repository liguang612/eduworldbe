package com.example.eduworldbe.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ReviewStatisticsResponse {
  private double averageScore;
  private Map<Integer, Long> scoreDistribution;
}