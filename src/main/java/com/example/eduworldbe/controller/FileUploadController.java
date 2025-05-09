package com.example.eduworldbe.controller;

import com.example.eduworldbe.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {
  @Autowired
  private FileUploadService fileUploadService;

  @PostMapping("/upload/{folder}")
  public Map<String, String> uploadFile(@PathVariable String folder, @RequestParam("file") MultipartFile file)
      throws IOException {
    String url = fileUploadService.uploadFile(file, folder);

    Map<String, String> response = new HashMap<>();
    response.put("url", url);
    return response;
  }
}
