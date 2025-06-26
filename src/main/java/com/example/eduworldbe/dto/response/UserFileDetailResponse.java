package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFileDetailResponse {
  private String id;
  private String fileName;
  private String fileUrl;
  private Long fileSize; // Size in bytes
  private String fileType;
  private Date uploadTime;
}