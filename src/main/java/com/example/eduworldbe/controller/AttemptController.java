package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Attempt;
import com.example.eduworldbe.model.Exam;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.AttemptService;
import com.example.eduworldbe.service.ExamService;
import com.example.eduworldbe.util.AuthUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {
  @Autowired
  private AttemptService attemptService;

  @Autowired
  private ExamService examService;

  @Autowired
  private AuthUtil authUtil;

  @GetMapping("/{id}")
  public ResponseEntity<Attempt> getAttempt(@PathVariable String id, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    Optional<Attempt> attemptOpt = attemptService.getById(id);
    if (attemptOpt.isPresent()) {
      Attempt attempt = attemptOpt.get();

      // Check if the attempt belongs to the current user
      if (!attempt.getUserId().equals(currentUser.getId())) {
        throw new AccessDeniedException("You are not authorized to view this attempt");
      }

      return ResponseEntity.ok(attempt);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/user")
  public ResponseEntity<List<Attempt>> getUserAttempts(HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    List<Attempt> attempts = attemptService.getByUserId(currentUser.getId());
    return ResponseEntity.ok(attempts);
  }

  @GetMapping("/exam/{examId}")
  public ResponseEntity<List<Attempt>> getUserExamAttempts(
      @PathVariable String examId, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    List<Attempt> attempts = attemptService.getByUserIdAndExamId(currentUser.getId(), examId);
    return ResponseEntity.ok(attempts);
  }

  @GetMapping("/exam/{examId}/active")
  public ResponseEntity<Attempt> getActiveExamAttempt(
      @PathVariable String examId, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    Optional<Attempt> activeAttemptOpt = attemptService.getActiveAttempt(currentUser.getId(), examId);
    if (activeAttemptOpt.isPresent()) {
      return ResponseEntity.ok(activeAttemptOpt.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{id}/submit")
  public ResponseEntity<Attempt> submitAttempt(
      @PathVariable String id, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    Optional<Attempt> attemptOpt = attemptService.getById(id);
    if (attemptOpt.isPresent()) {
      Attempt attempt = attemptOpt.get();

      // Check if the attempt belongs to the current user
      if (!attempt.getUserId().equals(currentUser.getId())) {
        throw new AccessDeniedException("You are not authorized to submit this attempt");
      }

      // Check if the attempt is already submitted
      if (attempt.getSubmitted()) {
        return ResponseEntity.ok(attempt); // Return the already submitted attempt
      }

      // Check if the exam is still open
      Optional<Exam> examOpt = examService.getById(attempt.getExamId());
      if (examOpt.isPresent()) {
        Exam exam = examOpt.get();
        Date now = new Date();

        if (exam.getCloseTime().before(now)) {
          return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body(attempt);
        }
      }

      Attempt submittedAttempt = attemptService.submitAttempt(id);
      return ResponseEntity.ok(submittedAttempt);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/exam/{examId}/leaderboard")
  public ResponseEntity<List<Attempt>> getExamLeaderboard(@PathVariable String examId) {
    List<Attempt> topAttempts = attemptService.getTopAttemptsByExam(examId);
    return ResponseEntity.ok(topAttempts);
  }
}