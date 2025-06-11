package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, String> {
  List<Lecture> findBySubjectId(String subjectId);

  @Query("SELECT DISTINCT l FROM Lecture l " +
      "INNER JOIN Chapter c ON l.id MEMBER OF c.lectureIds " +
      "INNER JOIN Course co ON c.courseId = co.id " +
      "WHERE :userId MEMBER OF co.studentIds")
  List<Lecture> findLecturesFromEnrolledCourses(@Param("userId") String userId);

  @Query("SELECT DISTINCT l FROM Lecture l " +
      "INNER JOIN Chapter c ON l.id MEMBER OF c.lectureIds " +
      "INNER JOIN Course co ON c.courseId = co.id " +
      "WHERE :userId MEMBER OF co.studentIds " +
      "AND (:subjectId IS NULL OR l.subjectId = :subjectId)")
  List<Lecture> findLecturesFromEnrolledCoursesWithSubject(
      @Param("userId") String userId,
      @Param("subjectId") String subjectId);
}
