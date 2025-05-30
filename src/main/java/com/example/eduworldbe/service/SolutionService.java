package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Solution;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.repository.SolutionRepository;
import com.example.eduworldbe.repository.QuestionRepository;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.dto.SolutionResponse;
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

@Service
public class SolutionService {
  @Autowired
  private SolutionRepository solutionRepository;

  @Autowired
  private QuestionRepository questionRepository;

  @Autowired
  private UserService userService;

  @Transactional
  public SolutionResponse create(Solution solution, User currentUser) {
    // Verify question exists
    Optional<Question> question = questionRepository.findById(solution.getQuestionId());
    if (question.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
    }

    solution.setCreatedBy(currentUser.getId());

    if (currentUser.getRole() == 1) {
      solution.setStatus(1);
    } else {
      solution.setStatus(0);
    }

    Solution savedSolution = solutionRepository.save(solution);
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
    Optional<Solution> solutionOpt = solutionRepository.findById(id);
    if (solutionOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solution not found");
    }

    Solution solution = solutionOpt.get();
    solution.setStatus(status);
    solution.setReviewedBy(reviewer.getId());
    solution.setReviewedAt(new Date());

    Solution savedSolution = solutionRepository.save(solution);
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