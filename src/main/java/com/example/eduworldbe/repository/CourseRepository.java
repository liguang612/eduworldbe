package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
	List<Course> findBySubjectId(String subjectId);

	List<Course> findByTeacherId(String teacherId);

	List<Course> findByTeacherIdAndSubjectId(String teacherId, String subjectId);

	@Query("SELECT c FROM Course c WHERE c.teacherId = :userId " +
			"ORDER BY (SELECT COALESCE(AVG(r.score), 0) FROM Review r WHERE r.targetType = 1 AND r.targetId = c.id) DESC")
	List<Course> findHighlightCoursesByTeacher(@Param("userId") String userId);

	@Query("SELECT c FROM Course c WHERE c.hidden = false " +
			"ORDER BY (SELECT COALESCE(AVG(r.score), 0) FROM Review r WHERE r.targetType = 1 AND r.targetId = c.id) DESC")
	List<Course> findHighlightCoursesForStudent();

	@Query("SELECT c FROM Course c WHERE c.hidden = false OR " +
			"EXISTS (SELECT 1 FROM c.studentIds s WHERE s = :userId) " +
			"ORDER BY (SELECT COALESCE(AVG(r.score), 0) FROM Review r WHERE r.targetType = 1 AND r.targetId = c.id) DESC")
	List<Course> findHighlightCoursesForEnrolledStudent(@Param("userId") String userId);

	@Query("SELECT c FROM Course c WHERE (c.teacherId = :userId OR " +
			":userId MEMBER OF c.teacherAssistantIds) " +
			"AND (:subjectId IS NULL OR c.subjectId = :subjectId)")
	List<Course> findTeacherCourses(@Param("userId") String userId, @Param("subjectId") String subjectId);

	@Query("SELECT c FROM Course c WHERE :userId MEMBER OF c.studentIds " +
			"AND (:subjectId IS NULL OR c.subjectId = :subjectId)")
	List<Course> findEnrolledCourses(@Param("userId") String userId, @Param("subjectId") String subjectId);

	@Query("SELECT c FROM Course c WHERE (c.hidden = false OR :userId MEMBER OF c.studentIds) " +
			"AND (:subjectId IS NULL OR c.subjectId = :subjectId)")
	List<Course> findAvailableCourses(@Param("userId") String userId, @Param("subjectId") String subjectId);

	@Query("SELECT c FROM Course c WHERE (c.teacherId = :userId OR " +
			":userId MEMBER OF c.teacherAssistantIds) " +
			"AND (:subjectId IS NULL OR c.subjectId = :subjectId)")
	List<Course> findTeacherCoursesWithFilters(@Param("userId") String userId,
			@Param("subjectId") String subjectId);

	@Query("SELECT c FROM Course c WHERE :userId MEMBER OF c.studentIds " +
			"AND (:subjectId IS NULL OR c.subjectId = :subjectId)")
	List<Course> findEnrolledCoursesWithFilters(@Param("userId") String userId,
			@Param("subjectId") String subjectId);

	@Query("SELECT c FROM Course c WHERE (c.hidden = false OR :userId MEMBER OF c.studentIds) " +
			"AND (:subjectId IS NULL OR c.subjectId = :subjectId)")
	List<Course> findAvailableCoursesWithFilters(@Param("userId") String userId,
			@Param("subjectId") String subjectId);

	@Query("SELECT DISTINCT c FROM Course c " +
			"INNER JOIN c.chapterIds ch " +
			"INNER JOIN Chapter chapter ON chapter.id = ch " +
			"WHERE :lectureId MEMBER OF chapter.lectureIds")
	List<Course> findCoursesContainingLecture(@Param("lectureId") String lectureId);
}
