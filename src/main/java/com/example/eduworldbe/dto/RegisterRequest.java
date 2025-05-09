package com.example.eduworldbe.dto;

import lombok.Data;

@Data
public class RegisterRequest {
  private String email;
  private String password;
  private String name;
  private String avatar; // url ảnh đại diện
  private String school;
  private Integer grade;
  private String address;
  private Integer role; // 0: student, 1: teacher, 2: assistant, 3: admin
  private String birthday; // nên để dạng String (yyyy-MM-dd), backend sẽ convert sang Date
  // các trường khác nếu cần
}
