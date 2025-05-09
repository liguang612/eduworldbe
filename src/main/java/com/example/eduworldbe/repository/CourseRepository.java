package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {
  List<Course> findBySubjectId(String subjectId);
}
