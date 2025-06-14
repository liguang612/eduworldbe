package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
  private String token;
  private String id;
  private String name;
  private String avatar;
  private Integer role;
}
