package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.SimpleGradeRequest;
import com.example.eduworldbe.dto.response.SimpleGradeResponse;
import com.example.eduworldbe.service.SimpleGradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/simple-grading")
public class SimpleGradingController {
  @Autowired
  private SimpleGradingService simpleGradingService;

  @PostMapping("/grade")
  public ResponseEntity<SimpleGradeResponse> gradeAnswers(@RequestBody SimpleGradeRequest request) {
    SimpleGradeResponse response = simpleGradingService.gradeAnswers(request);
    return ResponseEntity.ok(response);
  }
}