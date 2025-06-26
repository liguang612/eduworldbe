package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherStorageResponse {
  private String id;
  private String name;
  private String email;
  private String avatar;
  private Date birthday;
  private Long totalStorageUsed;
  private Integer fileCount;
  private Double searchScore;
  private Long storageLimit;
}