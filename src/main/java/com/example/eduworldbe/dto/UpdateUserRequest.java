package com.example.eduworldbe.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
  private String name;
  private String avatar;
  private String school;
  private Integer grade;
  private String address;
  private String birthday;
}