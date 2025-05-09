package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.service.CourseService;
import com.example.eduworldbe.dto.CourseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.model.User;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
  @Autowired
  private CourseService courseService;

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
  public List<CourseResponse> getAll(@RequestParam(required = false) String subjectId) {
    List<Course> courses = (subjectId == null || subjectId.isEmpty())
        ? courseService.getAll()
        : courseService.getBySubjectId(subjectId);
    return courses.stream()
        .map(courseService::toCourseResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public CourseResponse getById(@PathVariable String id) {
    return courseService.getById(id)
        .map(courseService::toCourseResponse)
        .orElse(null);
  }

  @PutMapping("/{id}")
  public Course update(@PathVariable String id, @RequestBody Course course) {
    return courseService.update(id, course);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable String id) {
    courseService.delete(id);
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
