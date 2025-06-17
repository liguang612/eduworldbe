package com.example.eduworldbe.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {
  private String email;
  private String password;
  private String name;
  private String avatar;
  private String school;
  private Integer grade;
  private String address;
  private Integer role;
  private String birthday;
}