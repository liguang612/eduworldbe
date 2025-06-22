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

  @Query("SELECT e FROM Exam e WHERE e.createdBy = :userId AND e.openTime > :currentTime ORDER BY e.openTime ASC")
  List<Exam> findUpcomingExamsByTeacher(@Param("userId") String userId, @Param("currentTime") Date currentTime);

  @Query("SELECT e FROM Exam e WHERE e.openTime > :currentTime ORDER BY e.openTime ASC")
  List<Exam> findAllUpcomingExams(@Param("currentTime") Date currentTime);

  @Query("SELECT e FROM Exam e " +
      "INNER JOIN Course c ON e.classId = c.id " +
      "WHERE :userId MEMBER OF c.studentIds")
  List<Exam> findExamsFromEnrolledCourses(@Param("userId") String userId);

  @Query("SELECT e FROM Exam e " +
      "INNER JOIN Course c ON e.classId = c.id " +
      "WHERE :userId MEMBER OF c.studentIds " +
      "AND (:subjectId IS NULL OR c.subjectId = :subjectId)")
  List<Exam> findExamsFromEnrolledCoursesWithSubject(
      @Param("userId") String userId,
      @Param("subjectId") String subjectId);
}