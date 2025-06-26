package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.AddLectureRequest;
import com.example.eduworldbe.model.Chapter;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.ChapterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.example.eduworldbe.util.AuthUtil;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {
  @Autowired
  private ChapterService chapterService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public Chapter create(@RequestBody Chapter chapter, HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    if (!authUtil.hasAccessToCourse(request, chapter.getCourseId())) {
      throw new RuntimeException("Unauthorized");
    }
    return chapterService.create(chapter);
  }

  @GetMapping("/course/{courseId}")
  public List<Chapter> getByCourseId(@PathVariable String courseId, HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    if (!authUtil.hasAccessToCourse(request, courseId)) {
      throw new RuntimeException("Unauthorized");
    }
    return chapterService.getByCourseId(courseId);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Chapter> getById(@PathVariable String id, HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    Optional<Chapter> chapter = chapterService.getById(id);
    if (chapter.isPresent()) {
      if (!authUtil.hasAccessToCourse(request, chapter.get().getCourseId())) {
        throw new RuntimeException("Unauthorized");
      }
      return ResponseEntity.ok(chapter.get());
    }
    return ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Chapter> update(@PathVariable String id, @RequestBody Chapter chapter,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    Optional<Chapter> existingChapter = chapterService.getById(id);
    if (existingChapter.isPresent()) {
      Chapter _chapter = existingChapter.get();

      if (!authUtil.hasAccessToCourse(request, _chapter.getCourseId())) {
        throw new RuntimeException("Unauthorized");
      }

      _chapter.setName(chapter.getName());
      return ResponseEntity.ok(chapterService.update(id, _chapter));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    Optional<Chapter> chapter = chapterService.getById(id);
    if (chapter.isPresent()) {
      if (!authUtil.hasAccessToCourse(request, chapter.get().getCourseId())) {
        throw new RuntimeException("Unauthorized");
      }
      chapterService.delete(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}/add-lecture")
  public ResponseEntity<Chapter> addLectureToChapter(
      @PathVariable String id,
      @RequestBody AddLectureRequest req,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    Optional<Chapter> chapter = chapterService.getById(id);
    if (chapter.isPresent()) {
      if (!authUtil.hasAccessToCourse(request, chapter.get().getCourseId())) {
        throw new RuntimeException("Unauthorized");
      }
      chapterService.addLectureToChapter(id, req.getLectureId());
      return ResponseEntity.ok(chapterService.getById(id).get());
    }
    return ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}/remove-lecture")
  public ResponseEntity<Chapter> removeLectureFromChapter(
      @PathVariable String id,
      @RequestBody AddLectureRequest req,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    Optional<Chapter> chapter = chapterService.getById(id);
    if (chapter.isPresent()) {
      if (!authUtil.hasAccessToCourse(request, chapter.get().getCourseId())) {
        throw new RuntimeException("Unauthorized");
      }
      chapterService.removeLectureFromChapter(id, req.getLectureId());
      return ResponseEntity.ok(chapterService.getById(id).get());
    }
    return ResponseEntity.notFound().build();
  }
}