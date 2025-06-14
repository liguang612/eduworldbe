package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Solution;
import com.example.eduworldbe.repository.QuestionRepository;
import com.example.eduworldbe.repository.SolutionRepository;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.dto.response.SolutionResponse;
import com.example.eduworldbe.model.Notification;
import com.example.eduworldbe.model.NotificationType;
import com.example.eduworldbe.model.Question;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;

@Service
public class SolutionService {
  @Autowired
  private SolutionRepository solutionRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private QuestionRepository questionRepository;

  @Transactional
  public SolutionResponse create(Solution solution, User currentUser) {
    solution.setCreatedBy(currentUser.getId());

    Solution savedSolution = solutionRepository.save(solution);

    Question question = questionRepository.findById(savedSolution.getQuestionId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

    if (currentUser.getRole() != null && currentUser.getRole() == 0) {
      solution.setStatus(0);

      try {
        notificationService.createNotification(Notification.builder()
            .userId(question.getCreatedBy())
            .type(NotificationType.NEW_SOLUTION_FOR_TEACHER_APPROVAL)
            .actorId(savedSolution.getCreatedBy())
            .questionId(savedSolution.getQuestionId())
            .solutionId(savedSolution.getId())
            .courseId(null));
      } catch (ExecutionException | InterruptedException e) {
        System.err.println("Failed to create NEW_SOLUTION_FOR_TEACHER_APPROVAL notification: " + e.getMessage());
        Thread.currentThread().interrupt();
      }
    } else {
      solution.setStatus(1);
    }

    return convertToResponse(savedSolution);
  }

  public Optional<SolutionResponse> getById(String id) {
    Optional<Solution> solutionOpt = solutionRepository.findById(id);
    return solutionOpt.map(this::convertToResponse);
  }

  public List<SolutionResponse> getByQuestionId(String questionId) {
    List<Solution> solutions = solutionRepository.findByQuestionId(questionId);
    return solutions.stream()
        .map(solution -> convertToResponse(solution))
        .collect(Collectors.toList());
  }

  public Page<SolutionResponse> getPendingSolutions(Pageable pageable) {
    Page<Solution> solutions = solutionRepository.findByStatus(0, pageable);
    return solutions.map(this::convertToResponse);
  }

  @Transactional
  public SolutionResponse reviewSolution(String id, Integer status, String reviewComment, User reviewer) {
    Solution solution = solutionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Solution not found"));

    solution.setStatus(status);
    solution.setReviewedBy(reviewer.getId());
    solution.setReviewedAt(new Date());

    Solution savedSolution = solutionRepository.save(solution);

    if (savedSolution.getCreatedBy() != null && !savedSolution.getCreatedBy().equals(reviewer.getId())) {
      NotificationType notificationType = null;
      if (status == 1) {
        notificationType = NotificationType.SOLUTION_ACCEPTED;
      } else if (status == 2) {
        notificationType = NotificationType.SOLUTION_REJECTED;
      }

      if (notificationType != null) {
        try {
          notificationService.createNotification(Notification.builder()
              .userId(savedSolution.getCreatedBy())
              .type(notificationType)
              .actorId(reviewer.getId())
              .questionId(savedSolution.getQuestionId())
              .solutionId(savedSolution.getId()));
        } catch (ExecutionException | InterruptedException e) {
          System.err.println("Failed to create SOLUTION_REVIEW notification for user " + savedSolution.getCreatedBy()
              + ": " + e.getMessage());
          Thread.currentThread().interrupt();
        }
      }
    }
    return convertToResponse(savedSolution);
  }

  @Transactional
  public void delete(String id) {
    Optional<Solution> solutionOpt = solutionRepository.findById(id);
    if (solutionOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solution not found");
    }
    solutionRepository.delete(solutionOpt.get());
  }

  // Helper method to convert Solution to SolutionResponse
  private SolutionResponse convertToResponse(Solution solution) {
    User creator = userService.findById(solution.getCreatedBy());
    if (creator == null) {
      // Handle case where creator is not found
      return SolutionResponse.fromSolution(solution, "Unknown", null, null, null, null);
    }
    return SolutionResponse.fromSolution(
        solution,
        creator.getName(),
        creator.getSchool(),
        creator.getGrade(),
        creator.getAvatar(),
        creator.getRole());
  }
}