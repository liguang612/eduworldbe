package com.example.eduworldbe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;

@Service
public class FileService {
  @Value("${file.upload-dir}")
  private String uploadDir;

  public String uploadFile(MultipartFile file, String directory) {
    try {
      String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
      Path uploadPath = Paths.get(uploadDir, directory);

      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      Path filePath = uploadPath.resolve(fileName);
      Files.copy(file.getInputStream(), filePath);

      return "/uploads/" + directory + "/" + fileName;
    } catch (Exception e) {
      System.out.println("Error uploading file: " + e.getMessage());
    }
    return null;
  }

  /**
   * Xóa file từ storage dựa trên URL
   * URL format: /uploads/images/abc.jpg
   */
  public void deleteFile(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty()) {
      return;
    }

    try {
      String relativePath = fileUrl.replaceFirst("^/uploads/", "");
      Path filePath = Paths.get(uploadDir, relativePath);

      if (Files.exists(filePath)) {
        Files.delete(filePath);
      }
    } catch (Exception e) {
      // Log error nhưng không throw exception để không ảnh hưởng đến luồng chính
      e.printStackTrace();
    }
  }

  /**
   * Xóa nhiều file cùng lúc
   */
  public void deleteFiles(List<String> fileUrls) {
    if (fileUrls == null) {
      return;
    }

    fileUrls.forEach(this::deleteFile);
  }

  public void deleteUnusedFiles(List<String> oldUrls, List<String> newUrls) {
    if (oldUrls == null || newUrls == null) {
      return;
    }

    oldUrls.stream()
        .filter(url -> !newUrls.contains(url))
        .forEach(this::deleteFile);
  }
}