package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.repository.CourseRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.dto.CourseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class CourseService {
  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReviewService reviewService;

  public Course create(Course course) {
    return courseRepository.save(course);
  }

  public List<Course> getAll(String userId, Integer userRole) {
    List<Course> allCourses = courseRepository.findAll();

    // If user is a teacher or TA, return all courses
    if (userRole == 1 || userRole == 2) {
      return allCourses;
    }

    // For students, filter out hidden courses they're not enrolled in
    return allCourses.stream()
        .filter(course -> !course.isHidden() ||
            (course.getStudentIds() != null && course.getStudentIds().contains(userId)))
        .collect(Collectors.toList());
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
    dto.setAvatar(course.getAvatar());
    dto.setSubjectId(course.getSubjectId());
    dto.setAllCategories(course.getAllCategories());
    dto.setLectureIds(course.getLectureIds());
    dto.setReviewIds(course.getReviewIds());
    dto.setHidden(course.isHidden());

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

    // Thêm averageRating và reviewCount
    double avg = reviewService.getAverageScore(1, course.getId());
    dto.setAverageRating(avg);

    return dto;
  }

  public List<Course> getBySubjectId(String subjectId) {
    return courseRepository.findBySubjectId(subjectId);
  }

  public List<Course> getByTeacherId(String teacherId) {
    return courseRepository.findByTeacherId(teacherId);
  }

  public List<Course> getByTeacherIdAndSubjectId(String teacherId, String subjectId) {
    return courseRepository.findByTeacherIdAndSubjectId(teacherId, subjectId);
  }

  public List<Course> getEnrolledCourses(String studentId) {
    return courseRepository.findAll().stream()
        .filter(course -> course.getStudentIds() != null && course.getStudentIds().contains(studentId))
        .toList();
  }

  public void addLectureToCourseLectureIds(String courseId, String lectureId) {
    Course course = getById(courseId).orElse(null);
    if (course != null) {
      if (course.getLectureIds() == null) {
        course.setLectureIds(new ArrayList<>());
      }
      course.getLectureIds().add(lectureId);
      update(courseId, course);
    }
  }

  public void removeLectureFromCourseLectureIds(String courseId, String lectureId) {
    Course course = getById(courseId).orElse(null);
    if (course != null && course.getLectureIds() != null) {
      course.getLectureIds().remove(lectureId);
      update(courseId, course);
    }
  }
}
