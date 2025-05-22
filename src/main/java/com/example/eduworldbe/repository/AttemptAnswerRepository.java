package com.example.eduworldbe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eduworldbe.model.AttemptAnswer;

@Repository
public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, String> {
  Optional<AttemptAnswer> findByAttemptIdAndQuestionId(String attemptId, String questionId);

  List<AttemptAnswer> findByAttemptId(String attemptId);
}