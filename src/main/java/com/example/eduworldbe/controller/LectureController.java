package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Lecture;
import com.example.eduworldbe.service.LectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.dto.LectureResponse;

@RestController
@RequestMapping("/api/lectures")
public class LectureController {
  @Autowired
  private LectureService lectureService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public Lecture create(@RequestBody Lecture lecture, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }
    lecture.setTeacherId(currentUser.getId());
    return lectureService.create(lecture);
  }

  @GetMapping("/{id}")
  public LectureResponse getById(@PathVariable String id, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    return lectureService.getById(id)
        .filter(lecture -> (currentUser.getRole() == 0 || currentUser.getId().equals(lecture.getTeacherId())))
        .map(lectureService::toLectureResponse)
        .orElseThrow(() -> new RuntimeException("Lecture not found or you do not have permission to view it"));
  }

  @GetMapping("/subject/{subjectId}")
  public List<LectureResponse> getBySubjectId(@PathVariable String subjectId) {
    return lectureService.getBySubjectId(subjectId).stream()
        .map(lectureService::toLectureResponse)
        .toList();
  }

  @GetMapping
  public List<LectureResponse> getAll(
      @RequestParam(required = false) String subjectId,
      @RequestParam(required = false) String keyword,
      HttpServletRequest request) {

    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    List<Lecture> allLectures;

    if (subjectId == null || subjectId.isEmpty()) {
      allLectures = lectureService.getAll();
    } else {
      allLectures = lectureService.getBySubjectId(subjectId);
    }

    List<Lecture> filteredLectures = allLectures.stream()
        .filter(lecture -> currentUser.getId().equals(lecture.getTeacherId()))
        .toList();

    if (keyword != null && !keyword.trim().isEmpty()) {
      filteredLectures = lectureService.searchLecturesByName(filteredLectures, keyword);
    }

    return filteredLectures.stream()
        .map(lectureService::toLectureResponse)
        .toList();
  }

  @PutMapping("/{id}")
  public Lecture update(@PathVariable String id, @RequestBody Lecture lecture, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    Lecture existingLecture = lectureService.getById(id)
        .orElseThrow(() -> new RuntimeException("Lecture not found"));

    if (!currentUser.getId().equals(existingLecture.getTeacherId())) {
      throw new RuntimeException("You do not have permission to update this lecture");
    }

    lecture.setId(id);
    return lectureService.update(id, lecture);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String id, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    Lecture existingLecture = lectureService.getById(id)
        .orElseThrow(() -> new RuntimeException("Lecture not found"));

    if (!currentUser.getId().equals(existingLecture.getTeacherId())) {
      throw new RuntimeException("You do not have permission to delete this lecture");
    }

    lectureService.delete(id);
  }

  @PutMapping("/{id}/add-question")
  public Lecture addEndQuestion(@PathVariable String id, @RequestBody AddQuestionRequest req,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    Lecture existingLecture = lectureService.getById(id)
        .orElseThrow(() -> new RuntimeException("Lecture not found"));

    if (!currentUser.getId().equals(existingLecture.getTeacherId())) {
      throw new RuntimeException("You do not have permission to add questions to this lecture");
    }

    return lectureService.addEndQuestion(id, req.getQuestionId());
  }

  @PutMapping("/{id}/remove-question")
  public Lecture removeEndQuestion(@PathVariable String id, @RequestBody AddQuestionRequest req,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    Lecture existingLecture = lectureService.getById(id)
        .orElseThrow(() -> new RuntimeException("Lecture not found"));

    if (!currentUser.getId().equals(existingLecture.getTeacherId())) {
      throw new RuntimeException("You do not have permission to remove questions from this lecture");
    }

    return lectureService.removeEndQuestion(id, req.getQuestionId());
  }

  @PostMapping("/by-ids")
  public List<LectureResponse> getByIds(@RequestBody List<String> ids, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    List<Lecture> ownedLectures = lectureService.getByIdsInOrder(ids);

    return ownedLectures.stream()
        .map(lectureService::toLectureResponse)
        .toList();
  }
}

class AddQuestionRequest {
  private String questionId;

  public String getQuestionId() {
    return questionId;
  }

  public void setQuestionId(String questionId) {
    this.questionId = questionId;
  }
}
