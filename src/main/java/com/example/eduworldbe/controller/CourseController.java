package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.service.CourseService;
import com.example.eduworldbe.service.FileService;
import com.example.eduworldbe.dto.CourseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
  @Autowired
  private CourseService courseService;

  @Autowired
  private FileService fileService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public Course create(@RequestBody Course course, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }
    course.setTeacherId(currentUser.getId());
    return courseService.create(course);
  }

  @GetMapping
  public List<CourseResponse> getAll(
      @RequestParam(required = false) String subjectId,
      @RequestParam(required = false) Boolean enrolled,
      @RequestParam(required = false) String keyword,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    List<Course> allCourses = courseService.getAll(); // Get all courses first
    List<Course> filteredCourses;

    if (currentUser.getRole() == 1) {
      // Đang xem những lớp học mình tạo | là TA
      filteredCourses = allCourses.stream()
          .filter(course -> currentUser.getId().equals(course.getTeacherId())
              || (course.getTeacherAssistantIds() != null
                  && course.getTeacherAssistantIds().contains(currentUser.getId())))
          .toList();

    } else {
      if (Boolean.TRUE.equals(enrolled)) {
        // Đang xem những lớp học mình được thêm vào
        filteredCourses = allCourses.stream()
            .filter(course -> course.getStudentIds() != null && course.getStudentIds().contains(currentUser.getId()))
            .toList();
      } else {
        // Đang xem những lớp học không bị ẩn | bị ẩn nhưng được thêm vào
        filteredCourses = allCourses.stream()
            .filter(course -> !course.isHidden()
                || (course.getStudentIds() != null && course.getStudentIds().contains(currentUser.getId())))
            .toList();
      }
    }

    // Apply subjectId filter if provided
    if (subjectId != null && !subjectId.isEmpty()) {
      filteredCourses = filteredCourses.stream()
          .filter(course -> subjectId.equals(course.getSubjectId()))
          .toList();
    }

    // Apply keyword search if provided
    if (keyword != null && !keyword.trim().isEmpty()) {
      filteredCourses = courseService.searchCoursesByName(filteredCourses, keyword);
    }

    return filteredCourses.stream()
        .map(courseService::toCourseResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CourseResponse> getById(@PathVariable String id) {
    Optional<Course> course = courseService.getById(id);
    if (course.isPresent()) {
      return ResponseEntity.ok(courseService.toCourseResponse(course.get()));
    }
    return ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Course> update(@PathVariable String id, @RequestBody Course course) {
    Optional<Course> existingCourse = courseService.getById(id);
    if (existingCourse.isPresent()) {
      course.setId(id);
      return ResponseEntity.ok(courseService.update(id, course));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    Optional<Course> course = courseService.getById(id);
    if (course.isPresent()) {
      courseService.delete(id);
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping("/{id}/avatar")
  public ResponseEntity<Course> uploadAvatar(
      @PathVariable String id,
      @RequestParam("file") MultipartFile file) {
    Optional<Course> course = courseService.getById(id);
    if (course.isPresent()) {
      // Delete old avatar if exists
      if (course.get().getAvatar() != null && !course.get().getAvatar().isEmpty()) {
        fileService.deleteFile(course.get().getAvatar());
      }

      String avatarUrl = fileService.uploadFile(file, "courses");
      Course updatedCourse = course.get();
      updatedCourse.setAvatar(avatarUrl);

      return ResponseEntity.ok(courseService.update(id, updatedCourse));
    }
    return ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}/add-member")
  public CourseResponse addMember(@PathVariable String id, @RequestBody AddMemberRequest req) {
    Course course = courseService.getById(id).orElseThrow();
    if (req.getRole() == 0) {
      course.getStudentIds().add(req.getUserId());
    } else if (req.getRole() == 1) {
      course.getTeacherAssistantIds().add(req.getUserId());
    }
    courseService.update(id, course);
    return courseService.toCourseResponse(course);
  }

  @PutMapping("/{id}/remove-member")
  public CourseResponse removeMember(@PathVariable String id, @RequestBody AddMemberRequest req) {
    Course course = courseService.getById(id).orElseThrow();
    if (req.getRole() == 0) {
      course.getStudentIds().remove(req.getUserId());
    } else if (req.getRole() == 1) {
      course.getTeacherAssistantIds().remove(req.getUserId());
    }
    courseService.update(id, course);
    return courseService.toCourseResponse(course);
  }

  @PostMapping("/{id}/request-join")
  public ResponseEntity<Integer> requestJoinCourse(
      @PathVariable String id,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    try {
      courseService.requestJoinCourse(id, currentUser.getId());
      return ResponseEntity.ok(200000);
    } catch (RuntimeException e) {
      if (e.getMessage().equals("already_enrolled")) {
        return ResponseEntity.ok(200001);
      } else if (e.getMessage().equals("already_requested")) {
        return ResponseEntity.ok(200002);
      }
      throw e;
    }
  }

  @PostMapping("/{id}/approve-join/{studentId}")
  public ResponseEntity<CourseResponse> approveJoinRequest(
      @PathVariable String id,
      @PathVariable String studentId,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    Course course = courseService.getById(id).orElseThrow();
    if (!currentUser.getId().equals(course.getTeacherId()) &&
        (course.getTeacherAssistantIds() == null ||
            !course.getTeacherAssistantIds().contains(currentUser.getId()))) {
      throw new RuntimeException("Unauthorized to approve join requests");
    }

    Course updatedCourse = courseService.approveJoinRequest(id, studentId);
    return ResponseEntity.ok(courseService.toCourseResponse(updatedCourse));
  }

  @PostMapping("/{id}/reject-join/{studentId}")
  public ResponseEntity<CourseResponse> rejectJoinRequest(
      @PathVariable String id,
      @PathVariable String studentId,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    // Kiểm tra quyền (chỉ giáo viên hoặc trợ giảng mới được từ chối)
    Course course = courseService.getById(id).orElseThrow();
    if (!currentUser.getId().equals(course.getTeacherId()) &&
        (course.getTeacherAssistantIds() == null ||
            !course.getTeacherAssistantIds().contains(currentUser.getId()))) {
      throw new RuntimeException("Unauthorized to reject join requests");
    }

    Course updatedCourse = courseService.rejectJoinRequest(id, studentId);
    return ResponseEntity.ok(courseService.toCourseResponse(updatedCourse));
  }
}

// DTO cho add/remove member
class AddMemberRequest {
  private String userId;
  private Integer role;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Integer getRole() {
    return role;
  }

  public void setRole(Integer role) {
    this.role = role;
  }
}

// DTO cho add/remove lecture

