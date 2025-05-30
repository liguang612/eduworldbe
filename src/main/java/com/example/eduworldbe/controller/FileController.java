package com.example.eduworldbe.controller;

import com.example.eduworldbe.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @deprecated This controller is deprecated. Please use FileUploadController
 *             instead.
 *             The new endpoint is still /api/files/upload but it uses Firebase
 *             Storage instead of local storage.
 */
@Deprecated
@RestController
@RequestMapping("/api/files/deprecated")
public class FileController {
  @Autowired
  private FileService fileService;

  @PostMapping("/upload")
  public ResponseEntity<?> uploadFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("type") String type) {
    String fileUrl = fileService.uploadFile(file, type);
    return ResponseEntity.ok().body(new FileUploadResponse(fileUrl));
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