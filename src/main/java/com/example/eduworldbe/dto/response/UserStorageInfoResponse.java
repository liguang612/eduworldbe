package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStorageInfoResponse {
  private String userId;
  private String userName;
  private String userEmail;
  private Long currentUsage;
  private Long storageLimit;
  private Double usagePercentage;
  private String formattedCurrentUsage;
  private String formattedStorageLimit;
  private Boolean isOverLimit;
}