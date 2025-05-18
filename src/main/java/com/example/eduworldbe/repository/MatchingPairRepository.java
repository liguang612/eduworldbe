package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.MatchingPair;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchingPairRepository extends JpaRepository<MatchingPair, String> {
  List<MatchingPair> findByQuestionId(String questionId);

  void deleteByQuestionId(String questionId);
}