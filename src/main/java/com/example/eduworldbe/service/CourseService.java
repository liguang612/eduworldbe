package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.model.Post;
import com.example.eduworldbe.model.Review;
import com.example.eduworldbe.repository.CourseRepository;
import com.example.eduworldbe.repository.ChapterRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.repository.PostRepository;
import com.example.eduworldbe.repository.CommentRepository;
import com.example.eduworldbe.repository.ReviewRepository;
import com.example.eduworldbe.dto.CourseResponse;
import com.example.eduworldbe.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.AbstractMap;
import java.util.ArrayList;

@Service
public class CourseService {
  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private ChapterRepository chapterRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReviewService reviewService;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private ReviewRepository reviewRepository;

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
    Course existingCourse = getById(id).orElseThrow(() -> new RuntimeException("Course not found"));

    if (updated.getName() != null) {
      existingCourse.setName(updated.getName());
    }
    if (updated.getDescription() != null) {
      existingCourse.setDescription(updated.getDescription());
    }
    if (updated.getAvatar() != null) {
      existingCourse.setAvatar(updated.getAvatar());
    }
    if (updated.getAllCategories() != null) {
      existingCourse.setAllCategories(updated.getAllCategories());
    }
    if (updated.getTeacherAssistantIds() != null) {
      existingCourse.setTeacherAssistantIds(updated.getTeacherAssistantIds());
    }
    if (updated.getStudentIds() != null) {
      existingCourse.setStudentIds(updated.getStudentIds());
    }
    if (updated.getChapterIds() != null) {
      existingCourse.setChapterIds(updated.getChapterIds());
    }
    if (updated.getReviewIds() != null) {
      existingCourse.setReviewIds(updated.getReviewIds());
    }
    existingCourse.setHidden(updated.isHidden());
    existingCourse.setAllowStudentPost(updated.isAllowStudentPost());
    existingCourse.setRequirePostApproval(updated.isRequirePostApproval());

    return courseRepository.save(existingCourse);
  }

  @Transactional
  public void delete(String id) {
    Optional<Course> courseOptional = courseRepository.findById(id);

    if (courseOptional.isPresent()) {
      Course courseToDelete = courseOptional.get();

      // 1. Delete all chapters and their lectures
      if (courseToDelete.getChapterIds() != null) {
        for (String chapterId : courseToDelete.getChapterIds()) {
          chapterRepository.deleteById(chapterId);
        }
      }

      // 2. Delete all posts and their comments
      List<Post> posts = postRepository.findByCourseId(courseToDelete.getId());
      for (Post post : posts) {
        // Delete all comments of the post
        if (post.getComments() != null) {
          commentRepository.deleteAll(post.getComments());
        }
        // Delete the post
        postRepository.delete(post);
      }

      // 3. Delete all reviews
      List<Review> reviews = reviewRepository.findByTargetTypeAndTargetId(1, courseToDelete.getId());
      reviewRepository.deleteAll(reviews);

      // 4. Finally delete the course
      courseRepository.deleteById(id);
    }
  }

  public CourseResponse toCourseResponse(Course course) {
    CourseResponse dto = new CourseResponse();
    dto.setId(course.getId());
    dto.setName(course.getName());
    dto.setAvatar(course.getAvatar());
    dto.setDescription(course.getDescription());
    dto.setSubjectId(course.getSubjectId());
    dto.setAllCategories(course.getAllCategories());
    dto.setChapters(course.getChapterIds() != null ? course.getChapterIds().stream()
        .map(id -> chapterRepository.findById(id).orElse(null))
        .filter(c -> c != null)
        .toList() : null);
    dto.setReviewIds(course.getReviewIds());
    dto.setHidden(course.isHidden());
    dto.setAllowStudentPost(course.isAllowStudentPost());
    dto.setRequirePostApproval(course.isRequirePostApproval());

    dto.setTeacher(course.getTeacherId() != null ? userRepository.findById(course.getTeacherId()).orElse(null) : null);

    if (course.getTeacherAssistantIds() != null) {
      dto.setTeacherAssistants(
          course.getTeacherAssistantIds().stream()
              .map(id -> userRepository.findById(id).orElse(null))
              .filter(u -> u != null)
              .toList());
    }

    if (course.getStudentIds() != null) {
      dto.setStudents(
          course.getStudentIds().stream()
              .map(id -> userRepository.findById(id).orElse(null))
              .filter(u -> u != null)
              .toList());
    }

    double avg = reviewService.getAverageScore(1, course.getId());
    dto.setAverageRating(avg);

    if (course.getPendingStudentIds() != null) {
      dto.setPendingStudents(
          course.getPendingStudentIds().stream()
              .map(id -> userRepository.findById(id).orElse(null))
              .filter(u -> u != null)
              .toList());
    }

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
    return getEnrolledCoursesOptimized(studentId, null);
  }

  public List<Course> getCoursesOptimized(String userId, Integer userRole, String subjectId, Boolean enrolled,
      String keyword) {
    String normalizedSubjectId = (subjectId != null && subjectId.trim().isEmpty()) ? null : subjectId;
    String normalizedKeyword = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;

    List<Course> filteredCourses;

    if (userRole != null && userRole == 1) {
      filteredCourses = courseRepository.findTeacherCoursesWithFilters(userId, normalizedSubjectId);
    } else {
      // Student role
      if (Boolean.TRUE.equals(enrolled)) {
        // Get enrolled courses only
        filteredCourses = courseRepository.findEnrolledCoursesWithFilters(userId, normalizedSubjectId);
      } else {
        // Get available courses (not hidden OR enrolled)
        filteredCourses = courseRepository.findAvailableCoursesWithFilters(userId, normalizedSubjectId);
      }
    }

    // Then apply fuzzy keyword search if provided
    if (normalizedKeyword != null) {
      filteredCourses = searchCoursesByName(filteredCourses, normalizedKeyword);
    }

    return filteredCourses;
  }

  public List<Course> getEnrolledCoursesOptimized(String studentId, String subjectId) {
    return courseRepository.findEnrolledCourses(studentId, subjectId);
  }

  public List<Course> searchCoursesByName(List<Course> courses, String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return courses;
    }

    String[] searchTerms = keyword.toLowerCase().split("\\s+");

    return courses.stream()
        .map(course -> {
          String courseName = course.getName().toLowerCase();
          List<String> categories = course.getAllCategories() != null
              ? course.getAllCategories().stream()
                  .map(String::toLowerCase)
                  .toList()
              : List.of();

          double score = 0.0;

          // Tính điểm cho mỗi từ khóa
          for (String term : searchTerms) {
            double termScore = 0.0;

            // Khớp chính xác
            if (courseName.equals(term)) {
              termScore += 100.0;
            } else if (courseName.contains(term)) {
              double lengthRatio = (double) term.length() / courseName.length();
              termScore += 80.0 * lengthRatio;
            }

            // Khớp với categories
            for (String category : categories) {
              if (category.equals(term)) {
                termScore += 60.0;
              } else if (category.contains(term)) {
                double lengthRatio = (double) term.length() / category.length();
                termScore += 40.0 * lengthRatio;
              }
            }

            // Levenshtein distance
            int distance = StringUtil.calculateLevenshteinDistance(courseName, term);
            if (distance <= 3) {
              termScore += Math.max(0, 30.0 * (1 - distance / 3.0));
            }

            // Kiểm tra Levenshtein distance cho từng từ trong tên
            String[] courseWords = courseName.split("\\s+");
            for (String word : courseWords) {
              distance = StringUtil.calculateLevenshteinDistance(word, term);
              if (distance <= 2) {
                termScore += Math.max(0, 20.0 * (1 - distance / 2.0));
              }
            }

            score += termScore;
          }

          score = score / searchTerms.length;

          return new AbstractMap.SimpleEntry<>(course, score);
        })
        .filter(entry -> entry.getValue() > 0)
        .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // Sắp xếp theo điểm giảm dần
        .map(AbstractMap.SimpleEntry::getKey)
        .toList();
  }

  public Course requestJoinCourse(String courseId, String studentId) {
    Course course = getById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

    if (course.getStudentIds() != null && course.getStudentIds().contains(studentId)) {
      throw new RuntimeException("already_requested");
    }

    if (course.getPendingStudentIds() != null && course.getPendingStudentIds().contains(studentId)) {
      throw new RuntimeException("already_enrolled");
    }

    if (course.getPendingStudentIds() == null) {
      course.setPendingStudentIds(new ArrayList<>());
    }
    course.getPendingStudentIds().add(studentId);

    return courseRepository.save(course);
  }

  public Course approveJoinRequest(String courseId, String studentId) {
    Course course = getById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

    if (course.getPendingStudentIds() == null || !course.getPendingStudentIds().remove(studentId)) {
      throw new RuntimeException("No pending request found for this student");
    }

    if (course.getStudentIds() == null) {
      course.setStudentIds(new ArrayList<>());
    }
    course.getStudentIds().add(studentId);

    return courseRepository.save(course);
  }

  public Course rejectJoinRequest(String courseId, String studentId) {
    Course course = getById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

    if (course.getPendingStudentIds() == null || !course.getPendingStudentIds().remove(studentId)) {
      throw new RuntimeException("No pending request found for this student");
    }

    return courseRepository.save(course);
  }

  public List<CourseResponse> getHighlightCourses(String userId, Integer userRole, Integer total) {
    List<Course> courses;

    if (userRole != null && userRole == 1) {
      courses = courseRepository.findHighlightCoursesByTeacher(userId);
    } else {
      courses = courseRepository.findHighlightCoursesForStudent();
    }

    return courses.stream()
        .map(this::toCourseResponse)
        .limit(total)
        .toList();
  }

  public List<Course> getCoursesContainingLecture(String lectureId) {
    return courseRepository.findCoursesContainingLecture(lectureId);
  }
}
