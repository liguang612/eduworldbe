package com.example.eduworldbe.service;

import com.example.eduworldbe.model.SharedMedia;
import com.example.eduworldbe.repository.SharedMediaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class SharedMediaService {
  @Autowired
  private SharedMediaRepository sharedMediaRepository;

  @Autowired
  private FileService fileService;

  public SharedMedia create(SharedMedia sharedMedia) {
    return sharedMediaRepository.save(sharedMedia);
  }

  public SharedMedia createWithFile(MultipartFile file, String title, Integer mediaType, String text)
      throws IOException {
    // Upload file using FileService
    String mediaUrl = fileService.uploadFile(file, "shared-media");

    // Create and save shared media
    SharedMedia sharedMedia = new SharedMedia();
    sharedMedia.setTitle(title);
    sharedMedia.setMediaType(mediaType);
    sharedMedia.setMediaUrl(mediaUrl);
    sharedMedia.setText(text);

    return sharedMediaRepository.save(sharedMedia);
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

  public void delete(String id) {
    SharedMedia media = sharedMediaRepository.findById(id).orElseThrow();
    // Delete the file if it exists
    if (media.getMediaUrl() != null) {
      fileService.deleteFile(media.getMediaUrl());
    }
    sharedMediaRepository.deleteById(id);
  }
}