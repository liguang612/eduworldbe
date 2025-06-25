package com.example.eduworldbe.dto.request;

import lombok.Data;

@Data
public class UserSearchRequest {
  private String name;
  private String email;
  private Integer role;
  private Boolean isActive;
  private Integer page = 0;
  private Integer size = 10;
}