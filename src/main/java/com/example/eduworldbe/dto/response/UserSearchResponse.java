package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponse {
  private String id;
  private String name;
  private String email;
  private String avatar;
  private String school;
  private Integer grade;
}