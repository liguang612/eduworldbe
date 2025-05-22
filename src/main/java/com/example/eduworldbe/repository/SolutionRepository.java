package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Solution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolutionRepository extends JpaRepository<Solution, String> {
  List<Solution> findByQuestionId(String questionId);

  List<Solution> findByCreatedBy(String createdBy);

  List<Solution> findByStatus(Integer status);

  Page<Solution> findByStatus(Integer status, Pageable pageable);

  Page<Solution> findByQuestionIdAndStatus(String questionId, Integer status, Pageable pageable);
}