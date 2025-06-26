package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherStorageListResponse {
  private List<TeacherStorageResponse> teachers;
  private Integer totalPages;
  private Long totalElements;
  private Integer currentPage;
  private Integer pageSize;
}