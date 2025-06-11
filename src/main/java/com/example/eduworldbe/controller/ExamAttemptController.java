package com.example.eduworldbe.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.eduworldbe.model.ExamAttempt;
import com.example.eduworldbe.service.ExamAttemptService;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.dto.ExamAttemptResponse;
import com.example.eduworldbe.dto.ExamAttemptListResponse;
import com.example.eduworldbe.dto.ExamAttemptDetailResponse;

import jakarta.servlet.http.HttpServletRequest;

import com.example.eduworldbe.model.User;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

// ExamAttemptController.java
@RestController
@RequestMapping("/api/exam-attempts")
@Slf4j
public class ExamAttemptController {
  @Autowired
  private ExamAttemptService examAttemptService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping("/{examId}/start")
  public ResponseEntity<ExamAttemptResponse> startAttempt(@PathVariable String examId, HttpServletRequest request) {
    User user = authUtil.getCurrentUser(request);
    if (user == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    ExamAttempt attempt = examAttemptService.startAttempt(user.getId(), examId);

    ExamAttemptResponse response = new ExamAttemptResponse();

    // Copy tất cả các trường từ attempt sang response
    response.setId(attempt.getId());
    response.setExamId(attempt.getExamId());
    response.setClassId(attempt.getClassId());
    response.setDuration(attempt.getDuration());
    response.setMaxScore(attempt.getMaxScore());
    response.setTitle(attempt.getTitle());
    response.setEasyScore(attempt.getEasyScore());
    response.setMediumScore(attempt.getMediumScore());
    response.setHardScore(attempt.getHardScore());
    response.setVeryHardScore(attempt.getVeryHardScore());
    response.setUserId(attempt.getUserId());
    response.setStartTime(attempt.getStartTime());
    response.setEndTime(attempt.getEndTime());
    response.setStatus(attempt.getStatus());
    response.setScore(attempt.getScore());
    response.setCreatedAt(attempt.getCreatedAt());
    response.setUpdatedAt(attempt.getUpdatedAt());
    response.setShuffleChoice(attempt.getShuffleChoice());
    response.setShuffleQuestion(attempt.getShuffleQuestion());

    if ("in_progress".equals(attempt.getStatus())) {
      response.setSavedAnswers(examAttemptService.getSavedAnswers(attempt.getId()));
    }

    return ResponseEntity.ok(response);
  }

  @PostMapping("/{attemptId}/answers/{questionId}")
  public ResponseEntity<Void> saveAnswer(
      @PathVariable String attemptId,
      @PathVariable String questionId,
      @RequestBody String answer) {
    examAttemptService.saveAnswer(attemptId, questionId, answer);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{attemptId}/submit")
  public ResponseEntity<Void> submitAttempt(
      @PathVariable String attemptId) {
    examAttemptService.submitAttempt(attemptId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{attemptId}")
  public ResponseEntity<Void> deleteAttempt(@PathVariable String attemptId) {
    examAttemptService.deleteAttempt(attemptId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/batch-delete")
  public ResponseEntity<Void> deleteAttempts(@RequestBody List<String> attemptIds) {
    examAttemptService.deleteAttempts(attemptIds);
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<ExamAttemptListResponse>> getAttempts(
      @RequestParam(required = false) String status,
      HttpServletRequest request) {
    User user = authUtil.getCurrentUser(request);
    if (user == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    List<ExamAttemptListResponse> attempts = examAttemptService.getAttemptsByUserAndStatus(user.getId(), status);
    return ResponseEntity.ok(attempts);
  }

  @GetMapping("/{attemptId}")
  public ResponseEntity<ExamAttemptDetailResponse> getAttemptDetail(
      @PathVariable String attemptId,
      HttpServletRequest request) {
    User user = authUtil.getCurrentUser(request);
    if (user == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    ExamAttemptDetailResponse attempt = examAttemptService.getAttemptDetail(attemptId);
    return ResponseEntity.ok(attempt);
  }

  @GetMapping("/exam/{examId}")
  public ResponseEntity<List<ExamAttemptListResponse>> getAttemptsByExamId(
      @PathVariable String examId,
      HttpServletRequest request) {
    User user = authUtil.getCurrentUser(request);
    if (user == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    List<ExamAttemptListResponse> attempts = examAttemptService.getAttemptsByExamId(examId);
    return ResponseEntity.ok(attempts);
  }
}