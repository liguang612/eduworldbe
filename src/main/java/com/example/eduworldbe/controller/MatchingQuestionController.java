package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.MatchingQuestionRequest;
import com.example.eduworldbe.service.MatchingQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matching-questions")
public class MatchingQuestionController {
  @Autowired
  private MatchingQuestionService matchingQuestionService;

  @PostMapping
  public ResponseEntity<MatchingQuestionService.MatchingQuestionResult> createMatchingQuestion(
      @RequestBody MatchingQuestionRequest request) {
    return ResponseEntity.ok(matchingQuestionService.createMatchingQuestion(request));
  }
}