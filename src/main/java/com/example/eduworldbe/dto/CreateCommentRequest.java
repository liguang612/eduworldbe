package com.example.eduworldbe.dto;

import lombok.Data;

@Data
public class CreateCommentRequest {
  private String postId;
  private String content;
}