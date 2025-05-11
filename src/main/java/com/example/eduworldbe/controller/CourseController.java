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
import java.util.ArrayList;
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
  public List<CourseResponse> getAll(@RequestParam(required = false) String subjectId, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    System.out.println("Searching with subjectId: " + subjectId);
    List<Course> courses;
    if (currentUser.getRole() == 1) { // Teacher role
      courses = (subjectId == null || subjectId.isEmpty())
          ? courseService.getByTeacherId(currentUser.getId())
          : courseService.getByTeacherIdAndSubjectId(currentUser.getId(), subjectId);
    } else { // Student role
      courses = (subjectId == null || subjectId.isEmpty())
          ? courseService.getAll(currentUser.getId(), currentUser.getRole())
          : courseService.getBySubjectId(subjectId).stream()
              .filter(course -> !course.isHidden() ||
                  (course.getStudentIds() != null && course.getStudentIds().contains(currentUser.getId())))
              .toList();
    }
    System.out.println("Found " + courses.size() + " courses");
    if (!courses.isEmpty()) {
      System.out.println("First course subjectId: " + courses.get(0).getSubjectId());
    }

    return courses.stream()
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
      course.getTAIds().add(req.getUserId());
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
      course.getTAIds().remove(req.getUserId());
    }
    courseService.update(id, course);
    return courseService.toCourseResponse(course);
  }

  @PutMapping("/{courseId}/add-lecture")
  public CourseResponse addLectureToCourse(@PathVariable String courseId, @RequestBody AddLectureRequest req) {
    Course course = courseService.getById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

    // Thêm lectureId vào danh sách
    if (course.getLectureIds() == null) {
      course.setLectureIds(new ArrayList<>());
    }

    if (!course.getLectureIds().contains(req.getLectureId())) {
      course.getLectureIds().add(req.getLectureId());
      courseService.update(courseId, course);
    }

    return courseService.toCourseResponse(course);
  }

  @PutMapping("/{courseId}/remove-lecture")
  public CourseResponse removeLectureFromCourse(@PathVariable String courseId, @RequestBody AddLectureRequest req) {
    Course course = courseService.getById(courseId).orElseThrow(() -> new RuntimeException("Course not found"));

    if (course.getLectureIds() != null) {
      course.getLectureIds().remove(req.getLectureId());
      courseService.update(courseId, course);
    }

    return courseService.toCourseResponse(course);
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
class AddLectureRequest {
  private String lectureId;

  public String getLectureId() {
    return lectureId;
  }

  public void setLectureId(String lectureId) {
    this.lectureId = lectureId;
  }
}
