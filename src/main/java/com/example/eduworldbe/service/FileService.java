package com.example.eduworldbe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
    } catch (IOException e) {
      throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
    }
  }

  public void deleteFile(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty()) {
      return;
    }

    try {
      // Remove leading slash and "uploads/" from the URL
      String relativePath = fileUrl.replaceFirst("^/uploads/", "");
      Path filePath = Paths.get(uploadDir, relativePath);

      if (Files.exists(filePath)) {
        Files.delete(filePath);
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not delete the file. Error: " + e.getMessage());
    }
  }
}