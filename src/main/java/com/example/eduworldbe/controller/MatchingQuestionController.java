package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.MatchingQuestionRequest;
import com.example.eduworldbe.service.MatchingQuestionService;
import com.example.eduworldbe.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/matching-questions")
public class MatchingQuestionController {
  @Autowired
  private MatchingQuestionService matchingQuestionService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public ResponseEntity<MatchingQuestionService.MatchingQuestionResult> createMatchingQuestion(
      @RequestBody MatchingQuestionRequest request, HttpServletRequest httpRequest) {
    authUtil.requireActiveUser(httpRequest);
    return ResponseEntity.ok(matchingQuestionService.createMatchingQuestion(request));
  }
}