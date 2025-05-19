package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, String> {
  List<Exam> findByClassId(String classId);

  List<Exam> findByCreatedBy(String createdBy);

  @Query("SELECT e FROM Exam e WHERE e.classId = :classId AND e.openTime <= :now AND e.closeTime >= :now")
  List<Exam> findActiveExamsByClassId(@Param("classId") String classId, @Param("now") Date now);

  @Query("SELECT e FROM Exam e WHERE e.classId = :classId AND e.closeTime < :now")
  List<Exam> findPastExamsByClassId(@Param("classId") String classId, @Param("now") Date now);

  @Query("SELECT e FROM Exam e WHERE e.classId = :classId AND e.openTime > :now")
  List<Exam> findUpcomingExamsByClassId(@Param("classId") String classId, @Param("now") Date now);
}