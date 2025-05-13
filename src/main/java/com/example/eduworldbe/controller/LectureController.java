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
  public LectureResponse getById(@PathVariable String id) {
    return lectureService.getById(id)
        .map(lectureService::toLectureResponse)
        .orElse(null);
  }

  @GetMapping("/subject/{subjectId}")
  public List<LectureResponse> getBySubjectId(@PathVariable String subjectId) {
    return lectureService.getBySubjectId(subjectId).stream()
        .map(lectureService::toLectureResponse)
        .toList();
  }

  @GetMapping
  public List<LectureResponse> getAll() {
    return lectureService.getAll().stream()
        .map(lectureService::toLectureResponse)
        .toList();
  }

  @PutMapping("/{id}")
  public Lecture update(@PathVariable String id, @RequestBody Lecture lecture) {
    lecture.setId(id);
    return lectureService.update(id, lecture);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String id) {
    lectureService.delete(id);
  }

  @PutMapping("/{id}/add-question")
  public Lecture addEndQuestion(@PathVariable String id, @RequestBody AddQuestionRequest req) {
    return lectureService.addEndQuestion(id, req.getQuestionId());
  }

  @PutMapping("/{id}/remove-question")
  public Lecture removeEndQuestion(@PathVariable String id, @RequestBody AddQuestionRequest req) {
    return lectureService.removeEndQuestion(id, req.getQuestionId());
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
