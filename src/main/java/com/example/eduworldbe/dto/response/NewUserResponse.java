package com.example.eduworldbe.dto.response;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserResponse {
  private String name;
  private String avatar;
  private String email;
  private String school;
  private Integer role;
  private Date createdAt;
}