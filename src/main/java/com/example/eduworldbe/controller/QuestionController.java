package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.service.QuestionService;
import com.example.eduworldbe.dto.QuestionDetailResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
  @Autowired
  private QuestionService questionService;

  @PostMapping
  public ResponseEntity<Question> create(@Valid @RequestBody Question question, HttpServletRequest request) {
    return ResponseEntity.ok(questionService.create(question, request));
  }

  @GetMapping("/{id}")
  public ResponseEntity<QuestionDetailResponse> getById(@PathVariable String id, HttpServletRequest request) {
    return questionService.getQuestionDetailById(id, request)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<Question>> getAll(
      @RequestParam(required = false) String createdBy,
      @RequestParam(required = false) String subjectId) {
    return ResponseEntity.ok(questionService.getAllFiltered(createdBy, subjectId));
  }

  @GetMapping("/type/{type}")
  public ResponseEntity<List<Question>> getByType(@PathVariable String type) {
    return ResponseEntity.ok(questionService.getByType(type));
  }

  @GetMapping("/created-by/{createdBy}")
  public ResponseEntity<List<Question>> getByCreatedBy(@PathVariable String createdBy) {
    return ResponseEntity.ok(questionService.getByCreatedBy(createdBy));
  }

  @GetMapping("/subject/{subjectId}")
  public ResponseEntity<List<Question>> getBySubjectId(@PathVariable String subjectId) {
    return ResponseEntity.ok(questionService.getBySubjectId(subjectId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Question> update(@PathVariable String id, @RequestBody Question updated) {
    return ResponseEntity.ok(questionService.update(id, updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    questionService.delete(id);
    return ResponseEntity.ok().build();
  }
}