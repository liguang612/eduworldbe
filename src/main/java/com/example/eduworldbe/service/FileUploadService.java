package com.example.eduworldbe.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import com.example.eduworldbe.dto.response.UserStorageInfoResponse;
import com.example.eduworldbe.exception.StorageLimitExceededException;
import com.example.eduworldbe.util.StorageUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

  @Autowired
  private StorageUsageService storageUsageService;

  public String uploadFile(MultipartFile file, String type, String userId) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File cannot be empty");
    }

    // Validate file type
    String contentType = file.getContentType();
    if (contentType == null) {
      throw new IllegalArgumentException("File content type cannot be null");
    }

    // Kiểm tra storage limit trước khi upload
    if (!storageUsageService.canUserUploadFile(userId, file.getSize())) {
      UserStorageInfoResponse storageInfo = storageUsageService.getUserStorageInfo(userId);
      throw new StorageLimitExceededException(String.format(
          "Giới hạn dung lượng media của bạn đã hết. (Đã sử dụng: %s, Giới hạn: %s). Không thể upload file có kích thước: %s. Hãy liên hệ với quản trị viên để có thể tăng giới hạn dung lượng media của bạn.",
          storageInfo.getFormattedCurrentUsage(),
          storageInfo.getFormattedStorageLimit(),
          StorageUtil.formatFileSize(file.getSize())));
    }

    String folder = mapTypeToFolder(type);
    Storage storage = StorageClient.getInstance().bucket().getStorage();
    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
    String path = folder + "/" + fileName;

    BlobInfo blobInfo = BlobInfo.newBuilder(StorageClient.getInstance().bucket(), path)
        .setContentType(contentType)
        .build();

    Blob blob = storage.create(blobInfo, file.getBytes());
    String fileUrl = blob.getMediaLink();
    String fileType = determineFileType(fileName, contentType);
    storageUsageService.recordFileUpload(userId, fileName, fileUrl, file.getSize(), fileType);
    return fileUrl;
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

        storageUsageService.deleteFileRecord(fileUrl);
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

  private String determineFileType(String fileName, String contentType) {
    if (contentType != null) {
      if (contentType.startsWith("image/"))
        return "image";
      if (contentType.startsWith("video/"))
        return "video";
      if (contentType.startsWith("audio/"))
        return "audio";
      if (contentType.equals("application/pdf"))
        return "pdf";
      if (contentType.startsWith("text/"))
        return "text";
    }

    // Fallback dựa trên extension
    String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    switch (extension) {
      case "jpg":
      case "jpeg":
      case "png":
      case "gif":
      case "webp":
        return "image";
      case "mp4":
      case "avi":
      case "mov":
      case "wmv":
        return "video";
      case "mp3":
      case "wav":
      case "ogg":
        return "audio";
      case "pdf":
        return "pdf";
      case "txt":
      case "doc":
      case "docx":
        return "text";
      default:
        return "other";
    }
  }

}
