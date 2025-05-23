package com.example.eduworldbe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eduworldbe.model.ExamAttempt;

@Repository
public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, String> {
  List<ExamAttempt> findByUserId(String userId);

  Optional<ExamAttempt> findByIdAndUserId(String id, String userId);

  Optional<ExamAttempt> findByUserIdAndExamIdAndStatus(String userId, String examId, String status);

  List<ExamAttempt> findByUserIdAndStatus(String userId, String status);

  long countByUserIdAndExamId(String userId, String examId);
}
