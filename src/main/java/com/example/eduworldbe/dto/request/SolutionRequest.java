package com.example.eduworldbe.dto.request;

import lombok.Data;

@Data
public class SolutionRequest {
  private String questionId;
  private String content;
}