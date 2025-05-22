package com.example.eduworldbe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eduworldbe.model.AttemptMatchingPair;

@Repository
public interface AttemptMatchingPairRepository extends JpaRepository<AttemptMatchingPair, String> {
  List<AttemptMatchingPair> findByQuestionId(String questionId);

  void deleteByQuestionId(String questionId);
}