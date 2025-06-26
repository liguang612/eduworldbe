package com.example.eduworldbe.util;

import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.service.CourseService;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CourseService courseService;

  public User getCurrentUser(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      String email = jwtUtil.extractEmail(token);
      return userRepository.findByEmail(email).orElse(null);
    }
    return null;
  }

  public boolean hasAccessToCourse(HttpServletRequest request, String courseId) {
    User currentUser = getCurrentUser(request);
    if (currentUser == null) {
      return false;
    }

    // Teacher has access to their own courses
    if (currentUser.getRole() == 1) {
      return courseService.getByTeacherId(currentUser.getId()).stream()
          .anyMatch(course -> course.getId().equals(courseId));
    }

    // Student has access to enrolled courses
    if (currentUser.getRole() == 0) {
      return courseService.getEnrolledCoursesOptimized(currentUser.getId(), null).stream()
          .anyMatch(course -> course.getId().equals(courseId));
    }

    return false;
  }

  public User requireActiveUser(HttpServletRequest request) {
    User currentUser = getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    } else if (!currentUser.getIsActive()) {
      throw new RuntimeException("Tài khoản đã bị vô hiệu hóa! Hãy liên hệ với quản trị viên để được hỗ trợ");
    }
    return currentUser;
  }
}
