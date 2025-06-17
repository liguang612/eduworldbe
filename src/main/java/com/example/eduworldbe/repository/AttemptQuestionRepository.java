package com.example.eduworldbe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eduworldbe.model.AttemptQuestion;

@Repository
public interface AttemptQuestionRepository extends JpaRepository<AttemptQuestion, String> {
  List<AttemptQuestion> findByAttemptId(String attemptId);

  Optional<AttemptQuestion> findByAttemptIdAndQuestionId(String attemptId, String questionId);
}
