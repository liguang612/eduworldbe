package com.example.eduworldbe.service;

import com.example.eduworldbe.model.SharedMedia;
import com.example.eduworldbe.repository.SharedMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class SharedMediaService {
  @Autowired
  private SharedMediaRepository sharedMediaRepository;

  @Autowired
  private FileUploadService fileUploadService;

  public SharedMedia create(SharedMedia sharedMedia) {
    sharedMedia.setUsageCount(0);
    return sharedMediaRepository.save(sharedMedia);
  }

  public SharedMedia createWithFile(MultipartFile file, String title, Integer mediaType, String text)
      throws IOException {
    String mediaUrl = fileUploadService.uploadFile(file, "shared-media");

    SharedMedia sharedMedia = new SharedMedia();
    sharedMedia.setTitle(title);
    sharedMedia.setMediaType(mediaType);
    sharedMedia.setMediaUrl(mediaUrl);
    sharedMedia.setText(text);
    sharedMedia.setUsageCount(0);

    return sharedMediaRepository.save(sharedMedia);
  }

  @Transactional
  public void incrementUsageCount(String id) {
    SharedMedia media = sharedMediaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("SharedMedia not found"));
    media.setUsageCount(media.getUsageCount() + 1);
    sharedMediaRepository.save(media);
  }

  @Transactional
  public void decrementUsageCount(String id) {
    SharedMedia media = sharedMediaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("SharedMedia not found"));
    if (media.getUsageCount() > 0) {
      media.setUsageCount(media.getUsageCount() - 1);
      sharedMediaRepository.save(media);
    }
  }

  public Optional<SharedMedia> getById(String id) {
    return sharedMediaRepository.findById(id);
  }

  public List<SharedMedia> getAll() {
    return sharedMediaRepository.findAll();
  }

  public List<SharedMedia> getByMediaType(Integer mediaType) {
    return sharedMediaRepository.findByMediaType(mediaType);
  }

  public List<SharedMedia> searchByTitle(String title) {
    return sharedMediaRepository.findByTitleContaining(title);
  }

  public SharedMedia update(String id, SharedMedia updated) {
    SharedMedia existing = sharedMediaRepository.findById(id).orElseThrow();
    if (updated.getTitle() != null)
      existing.setTitle(updated.getTitle());
    if (updated.getMediaUrl() != null)
      existing.setMediaUrl(updated.getMediaUrl());
    if (updated.getMediaType() != null)
      existing.setMediaType(updated.getMediaType());
    if (updated.getText() != null)
      existing.setText(updated.getText());
    return sharedMediaRepository.save(existing);
  }

  public SharedMedia updateWithFile(String id, MultipartFile file, String title, Integer mediaType, String text)
      throws IOException {
    SharedMedia existing = sharedMediaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("SharedMedia not found with id " + id));

    if (file != null && !file.isEmpty()) {
      // Delete old file if it exists
      if (existing.getMediaUrl() != null && !existing.getMediaUrl().isEmpty()) {
        fileUploadService.deleteFile(existing.getMediaUrl());
      }

      String mediaUrl = fileUploadService.uploadFile(file, "shared-media");
      existing.setMediaUrl(mediaUrl);
    }

    if (title != null) {
      existing.setTitle(title);
    }
    if (mediaType != null) {
      existing.setMediaType(mediaType);
    }
    if (text != null) {
      existing.setText(text);
    }

    return sharedMediaRepository.save(existing);
  }

  @Transactional
  public void delete(String id) {
    SharedMedia media = sharedMediaRepository.findById(id).orElseThrow();
    if (media.getUsageCount() > 0) {
      throw new RuntimeException("Cannot delete SharedMedia that is being used by questions");
    }
    // Delete the file if it exists
    if (media.getMediaUrl() != null) {
      fileUploadService.deleteFile(media.getMediaUrl());
    }
    sharedMediaRepository.deleteById(id);
  }
}