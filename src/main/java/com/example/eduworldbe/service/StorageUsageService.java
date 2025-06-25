package com.example.eduworldbe.service;

import com.example.eduworldbe.model.StorageUsage;
import com.example.eduworldbe.repository.StorageUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class StorageUsageService {

  @Autowired
  private StorageUsageRepository storageUsageRepository;

  public void recordFileUpload(String userId, String fileName, String fileUrl,
      Long fileSize, String fileType) {
    StorageUsage storageUsage = new StorageUsage();
    storageUsage.setUserId(userId);
    storageUsage.setFileName(fileName);
    storageUsage.setFileUrl(fileUrl);
    storageUsage.setFileSize(fileSize);
    storageUsage.setFileType(fileType);
    storageUsage.setUploadTime(new Date());
    storageUsageRepository.save(storageUsage);
  }

  public Long getTotalStorageUsedByUser(String userId) {
    Long totalSize = storageUsageRepository.getTotalStorageUsedByUser(userId);
    return totalSize != null ? totalSize : 0L;
  }

  public Long getTotalStorageUsed() {
    Long totalSize = storageUsageRepository.getTotalStorageUsed();
    return totalSize != null ? totalSize : 0L;
  }
}