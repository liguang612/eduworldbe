package com.example.eduworldbe.controller;

import com.example.eduworldbe.service.FileUploadService;
import com.example.eduworldbe.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
  @Autowired
  private FileUploadService fileUploadService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("type") String type,
      HttpServletRequest request) throws IOException {
    String userId = authUtil.requireActiveUser(request).getId();
    String url = fileUploadService.uploadFile(file, type, userId);
    return ResponseEntity.ok().body(new FileUploadResponse(url));
  }

  @DeleteMapping("/delete")
  public ResponseEntity<?> deleteFile(@RequestParam("url") String url, HttpServletRequest request) {
    authUtil.requireActiveUser(request);
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
