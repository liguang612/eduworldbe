package com.example.eduworldbe.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileUploadService {
  public String uploadFile(MultipartFile file, String folder) throws IOException {
    Storage storage = StorageClient.getInstance().bucket().getStorage();

    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
    String path = folder + "/" + fileName;

    BlobId blobId = BlobId.of("your-firebase-bucket.appspot.com", path);
    BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
        .setContentType(file.getContentType())
        .build();

    Blob blob = storage.create(blobInfo, file.getBytes());

    return blob.getMediaLink(); // URL đường dẫn đến file
  }
}
