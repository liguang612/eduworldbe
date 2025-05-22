package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Solution;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.repository.SolutionRepository;
import com.example.eduworldbe.repository.QuestionRepository;
import com.example.eduworldbe.model.User;
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

@Service
public class SolutionService {
  @Autowired
  private SolutionRepository solutionRepository;

  @Autowired
  private QuestionRepository questionRepository;

  @Transactional
  public Solution create(Solution solution, User currentUser) {
    // Verify question exists
    Optional<Question> question = questionRepository.findById(solution.getQuestionId());
    if (question.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
    }

    solution.setCreatedBy(currentUser.getId());
    solution.setStatus("PENDING");
    return solutionRepository.save(solution);
  }

  public Optional<Solution> getById(String id) {
    return solutionRepository.findById(id);
  }

  public List<Solution> getByQuestionId(String questionId) {
    return solutionRepository.findByQuestionId(questionId);
  }

  public Page<Solution> getPendingSolutions(Pageable pageable) {
    return solutionRepository.findByStatus("PENDING", pageable);
  }

  @Transactional
  public Solution reviewSolution(String id, String status, String reviewComment, User reviewer) {
    Optional<Solution> solutionOpt = solutionRepository.findById(id);
    if (solutionOpt.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Solution not found");
    }

    Solution solution = solutionOpt.get();
    solution.setStatus(status);
    solution.setReviewedBy(reviewer.getId());
    solution.setReviewedAt(new Date());

    return solutionRepository.save(solution);
  }
}