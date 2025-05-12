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
import java.util.Arrays;

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

  private int calculateLevenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];

    for (int i = 0; i <= s1.length(); i++) {
      dp[i][0] = i;
    }
    for (int j = 0; j <= s2.length(); j++) {
      dp[0][j] = j;
    }

    for (int i = 1; i <= s1.length(); i++) {
      for (int j = 1; j <= s2.length(); j++) {
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
        }
      }
    }

    return dp[s1.length()][s2.length()];
  }

  public List<Course> searchCoursesByName(List<Course> courses, String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return courses;
    }

    String[] searchTerms = keyword.toLowerCase().split("\\s+");

    return courses.stream()
        .filter(course -> {
          String courseName = course.getName().toLowerCase();
          List<String> categories = course.getAllCategories() != null
              ? course.getAllCategories().stream()
                  .map(String::toLowerCase)
                  .toList()
              : List.of();

          // Check if any search term matches the course name or categories
          return Arrays.stream(searchTerms)
              .anyMatch(term -> {
                // Check if term is a substring of course name
                if (courseName.contains(term)) {
                  return true;
                }

                // Check if term matches any category
                if (categories.stream().anyMatch(cat -> cat.contains(term))) {
                  return true;
                }

                // Check Levenshtein distance for fuzzy matching
                return calculateLevenshteinDistance(courseName, term) <= 5 ||
                    courseName.split("\\s+").length > 0 &&
                        Arrays.stream(courseName.split("\\s+"))
                            .anyMatch(word -> calculateLevenshteinDistance(word, term) <= 2);
              });
        })
        .toList();
  }
}
