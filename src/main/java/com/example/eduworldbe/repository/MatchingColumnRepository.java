package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.MatchingColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchingColumnRepository extends JpaRepository<MatchingColumn, String> {
  List<MatchingColumn> findByQuestionId(String questionId);

  List<MatchingColumn> findByQuestionIdAndSide(String questionId, String side);

  List<MatchingColumn> findByQuestionIdOrderByOrderIndexAsc(String questionId);

  void deleteByQuestionId(String questionId);
}