package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.service.QuestionService;
import com.example.eduworldbe.dto.CreateQuestionRequest;
import com.example.eduworldbe.dto.QuestionDetailResponse;
import com.example.eduworldbe.dto.QuestionListResponseItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import org.springframework.http.HttpStatus;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.model.User;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
  @Autowired
  private QuestionService questionService;

  @Autowired
  private AuthUtil authUtil;
  
  @PostMapping
  public ResponseEntity<Question> create(@Valid @RequestBody CreateQuestionRequest request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(questionService.create(request, httpRequest));
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

  @GetMapping("/shared-media/{sharedMediaId}")
  public ResponseEntity<List<QuestionListResponseItem>> getBySharedMediaId(@PathVariable String sharedMediaId,
      HttpServletRequest request) {
    return ResponseEntity.ok(questionService.getBySharedMediaId(sharedMediaId, request));
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

  @GetMapping("/search")
  public ResponseEntity<List<Question>> search(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String createdBy,
      @RequestParam(required = false) String subjectId) {
    List<Question> filteredQuestions = questionService.getAllFiltered(createdBy, subjectId);
    return ResponseEntity.ok(questionService.searchQuestions(filteredQuestions, keyword));
  }

  @PostMapping("/details")
  public ResponseEntity<?> getDetailsByIds(@RequestBody List<String> ids, HttpServletRequest request) {    
    try {
      if (ids == null) {
        return ResponseEntity.badRequest().body("Danh sách ID không được null");
      }
      
      if (ids.isEmpty()) {
        return ResponseEntity.badRequest().body("Danh sách ID không được để trống");
      }
      
      List<QuestionDetailResponse> result = questionService.getQuestionDetailsByIds(ids, request);

      return ResponseEntity.ok(result);
    } catch (Exception e) {
      System.out.println("Lỗi xử lý chi tiết: " + e.getClass().getName() + ": " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Lỗi xử lý: " + e.getMessage());
    }
  }
}