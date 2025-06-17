package com.example.eduworldbe.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {
  public String uploadFile(MultipartFile file, String type) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }

    // Validate file type
    String contentType = file.getContentType();
    if (contentType == null) {
      throw new IllegalArgumentException("File content type cannot be null");
    }

    // Map type to folder
    String folder = mapTypeToFolder(type);

    Storage storage = StorageClient.getInstance().bucket().getStorage();
    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
    String path = folder + "/" + fileName;

    BlobInfo blobInfo = BlobInfo.newBuilder(StorageClient.getInstance().bucket(), path)
        .setContentType(contentType)
        .build();

    Blob blob = storage.create(blobInfo, file.getBytes());
    return blob.getMediaLink();
  }

  public void deleteFile(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty()) {
      return;
    }

    try {
      // URL tải về từ FIrebase có 2 format kiểu như sau:
      // Format 1:
      // https://firebasestorage.googleapis.com/v0/b/[BUCKET]/o/[PATH]?alt=media&token=[TOKEN]
      // Format 2:
      // https://storage.googleapis.com/download/storage/v1/b/[BUCKET]/o/[PATH]?generation=...&alt=media

      String path;
      if (fileUrl.contains("/o/")) {
        // Extract path after /o/
        String pathPart = fileUrl.split("/o/")[1];
        // Remove query parameters
        path = pathPart.split("\\?")[0];
        // Decode URL encoding (e.g., %2F -> /)
        path = java.net.URLDecoder.decode(path, "UTF-8");
      } else {
        System.err.println("Unable to parse file URL: " + fileUrl);
        return;
      }

      Storage storage = StorageClient.getInstance().bucket().getStorage();
      Blob blob = storage.get(StorageClient.getInstance().bucket().getName(), path);

      if (blob != null) {
        blob.delete();
        System.out.println("Successfully deleted file: " + path);
      } else {
        System.out.println("File not found in storage: " + path);
      }
    } catch (Exception e) {
      System.err.println("Error deleting file: " + fileUrl);
      e.printStackTrace();
    }
  }

  // Xóa nhiều file cùng lúc

  @Async
  public void deleteFiles(List<String> fileUrls) {
    if (fileUrls == null) {
      return;
    }
    fileUrls.forEach(this::deleteFile);
  }

  // Xóa những file không còn được sử dụng
  public void deleteUnusedFiles(List<String> oldUrls, List<String> newUrls) {
    if (oldUrls == null || newUrls == null) {
      return;
    }

    oldUrls.stream()
        .filter(url -> !newUrls.contains(url))
        .forEach(this::deleteFile);
  }

  private String mapTypeToFolder(String type) {
    switch (type.toLowerCase()) {
      case "image":
        return "images";
      case "video":
        return "videos";
      case "audio":
        return "audio";
      case "document":
        return "documents";
      case "user":
        return "users";
      case "course":
        return "courses";
      case "post":
        return "posts";
      case "shared-media":
        return "shared-media";
      case "solution":
        return "solutions";
      default:
        return "others";
    }
  }
}
