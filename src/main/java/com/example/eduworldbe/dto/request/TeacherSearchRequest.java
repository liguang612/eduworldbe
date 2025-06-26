package com.example.eduworldbe.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherSearchRequest {
  private String name;
  private String email;
  private Integer page = 0;
  private Integer size = 10;

  // Kiểm tra xem có đang tìm kiếm hay không
  public boolean hasSearchCriteria() {
    return (name != null && !name.trim().isEmpty()) ||
        (email != null && !email.trim().isEmpty());
  }
}