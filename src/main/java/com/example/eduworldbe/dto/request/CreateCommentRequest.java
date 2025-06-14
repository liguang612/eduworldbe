package com.example.eduworldbe.dto.request;

import lombok.Data;

@Data
public class CreateCommentRequest {
  private String postId;
  private String content;
}