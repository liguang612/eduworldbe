package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AttemptRepository extends JpaRepository<Attempt, String> {
  List<Attempt> findByUserId(String userId);

  List<Attempt> findByExamId(String examId);

  List<Attempt> findByUserIdAndExamId(String userId, String examId);

  Optional<Attempt> findByUserIdAndExamIdAndSubmitted(String userId, String examId, Boolean submitted);

  @Query("SELECT a FROM Attempt a WHERE a.userId = :userId AND a.examId = :examId AND a.submitted = true ORDER BY a.score DESC")
  List<Attempt> findBestAttemptsByUserAndExam(@Param("userId") String userId, @Param("examId") String examId);

  @Query("SELECT a FROM Attempt a WHERE a.examId = :examId AND a.submitted = true ORDER BY a.score DESC")
  List<Attempt> findTopAttemptsByExam(@Param("examId") String examId);
}