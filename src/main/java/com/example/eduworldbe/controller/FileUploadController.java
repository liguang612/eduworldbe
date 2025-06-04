package com.example.eduworldbe.controller;

import com.example.eduworldbe.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
  @Autowired
  private FileUploadService fileUploadService;

  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("type") String type) throws IOException {
    String url = fileUploadService.uploadFile(file, type);
    return ResponseEntity.ok().body(new FileUploadResponse(url));
  }

  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteFile(@RequestParam("url") String url) {
    fileUploadService.deleteFile(url);
    return ResponseEntity.ok().build();
  }
}

class FileUploadResponse {
  private String url;

  public FileUploadResponse(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
