package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Solution;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.SolutionService;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.dto.SolutionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/solutions")
public class SolutionController {
  @Autowired
  private SolutionService solutionService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public ResponseEntity<Solution> create(@RequestBody SolutionRequest request, HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Solution solution = new Solution();
    solution.setQuestionId(request.getQuestionId());
    solution.setContent(request.getContent());

    return ResponseEntity.ok(solutionService.create(solution, currentUser));
  }

  @GetMapping("/question/{questionId}")
  public ResponseEntity<List<Solution>> getByQuestionId(@PathVariable String questionId) {
    return ResponseEntity.ok(solutionService.getByQuestionId(questionId));
  }

  @GetMapping("/pending")
  public ResponseEntity<Page<Solution>> getPendingSolutions(Pageable pageable, HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return ResponseEntity.ok(solutionService.getPendingSolutions(pageable));
  }

  @PostMapping("/{id}/review")
  public ResponseEntity<Solution> reviewSolution(
      @PathVariable String id,
      @RequestParam String status,
      @RequestParam(required = false) String reviewComment,
      HttpServletRequest httpRequest) {
    User currentUser = authUtil.getCurrentUser(httpRequest);
    if (currentUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return ResponseEntity.ok(solutionService.reviewSolution(id, status, reviewComment, currentUser));
  }
}