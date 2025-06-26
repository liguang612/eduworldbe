package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
  private String id;
  private String email;
  private String name;
  private String avatar;
  private String school;
  private Integer grade;
  private String address;
  private Integer role;
  private String birthday;
  private String createdAt;
  private Boolean isActive;
  private Double searchScore; // Điểm tìm kiếm (cho fuzzy search)
  private Long storageLimit;
}