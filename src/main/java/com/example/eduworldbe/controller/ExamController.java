package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Exam;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.ExamService;
import com.example.eduworldbe.service.QuestionService;
import com.example.eduworldbe.dto.ExamResponse;
import com.example.eduworldbe.dto.QuestionDetailResponse;
import com.example.eduworldbe.util.AuthUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
  @Autowired
  private ExamService examService;

  @Autowired
  private AuthUtil authUtil;

  @Autowired
  private QuestionService questionService;

  @PostMapping
  public ResponseEntity<Exam> createExam(@RequestBody Exam exam, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    // Set the creator of the exam
    exam.setCreatedBy(currentUser.getId());

    Exam createdExam = examService.create(exam);
    return new ResponseEntity<>(createdExam, HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ExamResponse> getExamById(@PathVariable String id) {
    Optional<Exam> examOpt = examService.getById(id);
    if (examOpt.isPresent()) {
      ExamResponse response = examService.toExamResponse(examOpt.get());
      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/class/{classId}")
  public ResponseEntity<List<ExamResponse>> getExamsByClassId(@PathVariable String classId) {
    List<Exam> exams = examService.getByClassId(classId);
    List<ExamResponse> responses = exams.stream()
        .map(exam -> examService.toExamResponse(exam))
        .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/teacher")
  public ResponseEntity<List<ExamResponse>> getExamsByTeacher(HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    List<Exam> exams = examService.getByCreatedBy(currentUser.getId());
    List<ExamResponse> responses = exams.stream()
        .map(exam -> examService.toExamResponse(exam))
        .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @PutMapping("/{id}")
  public ResponseEntity<Exam> updateExam(@PathVariable String id, @RequestBody Exam exam,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    Optional<Exam> existingExamOpt = examService.getById(id);
    if (existingExamOpt.isPresent()) {
      Exam existingExam = existingExamOpt.get();

      // Check if the current user is the creator of the exam
      if (!existingExam.getCreatedBy().equals(currentUser.getId())) {
        throw new AccessDeniedException("You are not authorized to update this exam");
      }

      // Update the exam
      Exam updatedExam = examService.update(id, exam);
      return ResponseEntity.ok(updatedExam);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteExam(@PathVariable String id, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    Optional<Exam> existingExamOpt = examService.getById(id);
    if (existingExamOpt.isPresent()) {
      Exam existingExam = existingExamOpt.get();

      // Check if the current user is the creator of the exam
      if (!existingExam.getCreatedBy().equals(currentUser.getId())) {
        throw new AccessDeniedException("You are not authorized to delete this exam");
      }

      // Delete the exam
      examService.delete(id);
      return ResponseEntity.noContent().build();
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{examId}/questions/{questionId}")
  public ResponseEntity<Exam> addQuestionToExam(@PathVariable String examId,
      @PathVariable String questionId,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    Optional<Exam> existingExamOpt = examService.getById(examId);
    if (existingExamOpt.isPresent()) {
      Exam existingExam = existingExamOpt.get();

      // Check if the current user is the creator of the exam
      if (!existingExam.getCreatedBy().equals(currentUser.getId())) {
        throw new AccessDeniedException("You are not authorized to modify this exam");
      }

      // Add the question to the exam
      Exam updatedExam = examService.addQuestionToExam(examId, questionId);
      return ResponseEntity.ok(updatedExam);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @DeleteMapping("/{examId}/questions/{questionId}")
  public ResponseEntity<Exam> removeQuestionFromExam(@PathVariable String examId,
      @PathVariable String questionId,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    Optional<Exam> existingExamOpt = examService.getById(examId);
    if (existingExamOpt.isPresent()) {
      Exam existingExam = existingExamOpt.get();

      // Check if the current user is the creator of the exam
      if (!existingExam.getCreatedBy().equals(currentUser.getId())) {
        throw new AccessDeniedException("You are not authorized to modify this exam");
      }

      // Remove the question from the exam
      Exam updatedExam = examService.removeQuestionFromExam(examId, questionId);
      return ResponseEntity.ok(updatedExam);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/{examId}/questions")
  public ResponseEntity<Exam> addQuestionsToExam(@PathVariable String examId,
      @RequestBody List<String> questionIds,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    Optional<Exam> existingExamOpt = examService.getById(examId);
    if (existingExamOpt.isPresent()) {
      Exam existingExam = existingExamOpt.get();

      // Check if the current user is the creator of the exam
      if (!existingExam.getCreatedBy().equals(currentUser.getId())) {
        throw new AccessDeniedException("You are not authorized to modify this exam");
      }

      // Add the questions to the exam
      Exam updatedExam = examService.addQuestionsToExam(examId, questionIds);
      return ResponseEntity.ok(updatedExam);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/{examId}/questions")
  public ResponseEntity<List<Question>> getExamQuestions(@PathVariable String examId) {
    List<Question> questions = examService.getExamQuestions(examId);
    return ResponseEntity.ok(questions);
  }

  @GetMapping("/{examId}/questions/details")
  public ResponseEntity<?> getExamQuestionsDetails(@PathVariable String examId, HttpServletRequest request) {
    Optional<Exam> examOpt = examService.getById(examId);
    if (examOpt.isPresent()) {
      Exam exam = examOpt.get();
      Map<String, Object> response = new HashMap<>();

      // Add exam information
      response.put("exam", examService.toExamResponse(exam));

      // Add list of detailed questions
      if (exam.getQuestionIds() != null && !exam.getQuestionIds().isEmpty()) {
        List<QuestionDetailResponse> questions = questionService.getQuestionDetailsByIds(exam.getQuestionIds(),
            request);
        response.put("questions", questions);
      } else {
        response.put("questions", new ArrayList<>());
      }

      return ResponseEntity.ok(response);
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/{examId}/questions/generate")
  public ResponseEntity<List<Question>> generateExamQuestions(@PathVariable String examId) {
    List<Question> questions = examService.generateExamQuestions(examId);
    return ResponseEntity.ok(questions);
  }

  @GetMapping("/upcoming")
  public List<ExamResponse> getUpcomingExams(
      @RequestParam(required = false, defaultValue = "10") Integer total,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }
    return examService.getUpcomingExams(currentUser.getId(), currentUser.getRole(), total);
  }
}