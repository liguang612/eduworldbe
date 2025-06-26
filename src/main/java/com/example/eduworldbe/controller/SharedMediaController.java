package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.SharedMedia;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.SharedMediaService;
import com.example.eduworldbe.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/shared-media")
public class SharedMediaController {
  @Autowired
  private SharedMediaService sharedMediaService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public ResponseEntity<SharedMedia> create(@RequestBody SharedMedia sharedMedia) {
    return ResponseEntity.ok(sharedMediaService.create(sharedMedia));
  }

  @PostMapping("/upload")
  public ResponseEntity<SharedMedia> createWithFile(
      @RequestParam("file") MultipartFile file,
      @RequestParam("title") String title,
      @RequestParam("mediaType") Integer mediaType,
      @RequestParam(value = "text", required = false) String text) throws IOException {
    return ResponseEntity.ok(sharedMediaService.createWithFile(file, title, mediaType, text));
  }

  @GetMapping("/{id}")
  public ResponseEntity<SharedMedia> getById(@PathVariable String id) {
    return sharedMediaService.getById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<SharedMedia>> getAll(@RequestParam Integer mediaType, @RequestParam String userId,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);
    if (currentUser.getRole() == 0) {
      return ResponseEntity.badRequest().body(null);
    }
    return ResponseEntity.ok(sharedMediaService.getAll(mediaType, currentUser.getId()));
  }

  @GetMapping("/type/{mediaType}")
  public ResponseEntity<List<SharedMedia>> getByMediaType(@PathVariable Integer mediaType) {
    return ResponseEntity.ok(sharedMediaService.getByMediaType(mediaType));
  }

  @GetMapping("/search")
  public ResponseEntity<List<SharedMedia>> searchByTitle(@RequestParam String title) {
    return ResponseEntity.ok(sharedMediaService.searchByTitle(title));
  }

  @PostMapping("/upload/{id}")
  public ResponseEntity<SharedMedia> updateWithFile(
      @PathVariable String id,
      @RequestParam(value = "file", required = false) MultipartFile file,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "mediaType", required = false) Integer mediaType,
      @RequestParam(value = "text", required = false) String text) throws IOException {
    return ResponseEntity.ok(sharedMediaService.updateWithFile(id, file, title, mediaType, text));
  }

  @PutMapping("/{id}")
  public ResponseEntity<SharedMedia> update(@PathVariable String id, @RequestBody SharedMedia updated) {
    return ResponseEntity.ok(sharedMediaService.update(id, updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    sharedMediaService.delete(id);
    return ResponseEntity.ok().build();
  }
}