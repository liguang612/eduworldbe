package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.repository.CourseRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.dto.CourseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private UserRepository userRepository;

  public Course create(Course course) {
    return courseRepository.save(course);
  }

  public List<Course> getAll() {
    return courseRepository.findAll();
  }

  public Optional<Course> getById(String id) {
    return courseRepository.findById(id);
  }

  public Course update(String id, Course updated) {
    updated.setId(id);
    return courseRepository.save(updated);
  }

  public void delete(String id) {
    courseRepository.deleteById(id);
  }

  public CourseResponse toCourseResponse(Course course) {
    CourseResponse dto = new CourseResponse();
    dto.setId(course.getId());
    dto.setName(course.getName());
    dto.setSubjectId(course.getSubjectId());
    dto.setAllCategories(course.getAllCategories());
    dto.setLectureIds(course.getLectureIds());
    dto.setReviewIds(course.getReviewIds());

    // Lấy teacher
    dto.setTeacher(course.getTeacherId() != null ? userRepository.findById(course.getTeacherId()).orElse(null) : null);

    // Lấy trợ giảng
    if (course.getTAIds() != null) {
      dto.setTeacherAssistants(
          course.getTAIds().stream()
              .map(id -> userRepository.findById(id).orElse(null))
              .filter(u -> u != null)
              .toList());
    }

    // Lấy học sinh
    if (course.getStudentIds() != null) {
      dto.setStudents(
          course.getStudentIds().stream()
              .map(id -> userRepository.findById(id).orElse(null))
              .filter(u -> u != null)
              .toList());
    }

    return dto;
  }

  public List<Course> getBySubjectId(String subjectId) {
    return courseRepository.findBySubjectId(subjectId);
  }
}
