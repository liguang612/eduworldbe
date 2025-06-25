package com.example.eduworldbe.dto.request;

import lombok.Data;

@Data
public class ChangeUserRoleRequest {
  private Integer role; // 0: student, 1: teacher, 2: admin
}