package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.CourseJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseJoinRequestRepository extends JpaRepository<CourseJoinRequest, String> {
  List<CourseJoinRequest> findByCourseIdAndStatus(String courseId, Integer status);

  List<CourseJoinRequest> findByUserId(String userId);

  List<CourseJoinRequest> findByCourseId(String courseId);
}
