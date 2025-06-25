package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUsageResponse {
  private Long totalStorageUsed;
  private List<UserStorageData> userStorageData;
  private List<FileTypeStorageData> fileTypeStorageData;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserStorageData {
    private String userId;
    private String userEmail;
    private Long totalSize;
    private Long fileCount;
    private String formattedSize;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FileTypeStorageData {
    private String fileType;
    private Long totalSize;
    private Long fileCount;
    private String formattedSize;
  }
}