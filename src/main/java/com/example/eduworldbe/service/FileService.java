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

  public String uploadFile(MultipartFile file, String subDirectory) {
    try {
      // Create directory if it doesn't exist
      Path uploadPath = Paths.get(uploadDir, subDirectory);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      // Generate unique filename
      String originalFilename = file.getOriginalFilename();
      String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      String filename = UUID.randomUUID().toString() + extension;

      // Save file
      Path filePath = uploadPath.resolve(filename);
      Files.copy(file.getInputStream(), filePath);

      // Return the relative URL path
      return "/uploads/" + subDirectory + "/" + filename;
    } catch (IOException e) {
      throw new RuntimeException("Failed to store file", e);
    }
  }
}