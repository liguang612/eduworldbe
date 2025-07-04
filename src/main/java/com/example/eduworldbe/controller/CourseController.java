package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.service.CourseService;
import com.example.eduworldbe.service.FavouriteService;
import com.example.eduworldbe.service.FileUploadService;
import com.example.eduworldbe.dto.request.AddMemberRequest;
import com.example.eduworldbe.dto.response.CourseResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
  @Autowired
  private AuthUtil authUtil;

  @Autowired
  private CourseService courseService;

  @Autowired
  private FileUploadService fileUploadService;

  @Autowired
  private FavouriteService favouriteService;

  @PostMapping
  public Course create(@RequestBody Course course, HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    course.setTeacherId(currentUser.getId());
    return courseService.create(course);
  }

  @GetMapping
  public List<CourseResponse> getAll(
      @RequestParam(required = false) String subjectId,
      @RequestParam(required = false) Boolean enrolled,
      @RequestParam(required = false) String keyword,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    List<Course> filteredCourses = courseService.getCoursesOptimized(
        currentUser.getId(),
        currentUser.getRole(),
        subjectId,
        enrolled,
        keyword);

    return filteredCourses.stream()
        .map(courseService::toCourseResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CourseResponse> getById(@PathVariable String id, HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    Optional<Course> course = courseService.getById(id);
    if (course.isPresent()) {
      Course _course = course.get();

      if (currentUser.getRole() == 0) {
        // Nếu là học sinh và không phải là học sinh của lớp
        if (!_course.getStudentIds().contains(currentUser.getId())) {
          // Nếu lớp ẩn thì trả về 404
          if (_course.isHidden()) {
            return ResponseEntity.notFound().build();
          } else {
            // Nếu lớp công khai thì trả về 403
            return ResponseEntity.status(403).body(null);
          }
        }
      } else {
        // Nếu giáo viên không phải trợ giảng cũng không phải giáo viên tạo lớp luôn.
        if (!_course.getTeacherAssistantIds().contains(currentUser.getId())
            && !currentUser.getId().equals(_course.getTeacherId())) {
          return ResponseEntity.notFound().build();
        }
      }

      CourseResponse courseResponse = courseService.toCourseResponse(_course);
      if (currentUser.getRole() == 0) {
        courseResponse.setFavourite(favouriteService.isFavourited(1, id, currentUser.getId()));
      }
      return ResponseEntity.ok(courseResponse);
    }
    return ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Course> update(@PathVariable String id, @RequestBody Course course,
      HttpServletRequest request) {
    authUtil.requireActiveUser(request);

    Optional<Course> existingCourse = courseService.getById(id);
    if (existingCourse.isPresent()) {
      course.setId(id);
      return ResponseEntity.ok(courseService.update(id, course));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
    authUtil.requireActiveUser(request);
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
      @RequestParam("file") MultipartFile file,
      HttpServletRequest request) throws IOException {
    User currentUser = authUtil.requireActiveUser(request);

    Optional<Course> course = courseService.getById(id);
    if (course.isPresent()) {
      // Delete old avatar if exists
      if (course.get().getAvatar() != null && !course.get().getAvatar().isEmpty()) {
        fileUploadService.deleteFile(course.get().getAvatar());
      }

      String avatarUrl = fileUploadService.uploadFile(file, "course", currentUser.getId());
      Course updatedCourse = course.get();
      updatedCourse.setAvatar(avatarUrl);

      return ResponseEntity.ok(courseService.update(id, updatedCourse));
    }
    return ResponseEntity.notFound().build();
  }

  @PutMapping("/{id}/add-member")
  public CourseResponse addMember(@PathVariable String id, @RequestBody AddMemberRequest req,
      HttpServletRequest request) {
    authUtil.requireActiveUser(request);

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
  public CourseResponse removeMember(@PathVariable String id, @RequestBody AddMemberRequest req,
      HttpServletRequest request) {
    authUtil.requireActiveUser(request);

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
    User currentUser = authUtil.requireActiveUser(request);

    try {
      courseService.requestJoinCourse(id, currentUser.getId());
      return ResponseEntity.ok(200000);
    } catch (RuntimeException e) {
      if (e.getMessage().equals("already_requested")) {
        return ResponseEntity.ok(200001);
      } else if (e.getMessage().equals("already_enrolled")) {
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
    User currentUser = authUtil.requireActiveUser(request);

    Course course = courseService.getById(id).orElseThrow();
    if (!currentUser.getId().equals(course.getTeacherId()) &&
        (course.getTeacherAssistantIds() == null ||
            !course.getTeacherAssistantIds().contains(currentUser.getId()))) {
      throw new RuntimeException("Unauthorized to approve join requests");
    }

    Course updatedCourse = courseService.approveJoinRequest(id, studentId, currentUser.getId());
    return ResponseEntity.ok(courseService.toCourseResponse(updatedCourse));
  }

  @PostMapping("/{id}/reject-join/{studentId}")
  public ResponseEntity<CourseResponse> rejectJoinRequest(
      @PathVariable String id,
      @PathVariable String studentId,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    Course course = courseService.getById(id).orElseThrow();
    if (!currentUser.getId().equals(course.getTeacherId()) &&
        (course.getTeacherAssistantIds() == null ||
            !course.getTeacherAssistantIds().contains(currentUser.getId()))) {
      throw new RuntimeException("Unauthorized to reject join requests");
    }

    Course updatedCourse = courseService.rejectJoinRequest(id, studentId, currentUser.getId());
    return ResponseEntity.ok(courseService.toCourseResponse(updatedCourse));
  }

  @GetMapping("/teacher/{teacherId}")
  public List<CourseResponse> getByTeacherId(@PathVariable String teacherId) {
    return courseService.getByTeacherId(teacherId).stream()
        .map(courseService::toCourseResponse)
        .toList();
  }

  @GetMapping("/highlight")
  public List<CourseResponse> getHighlightCourses(
      @RequestParam(required = false, defaultValue = "10") Integer total,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    return courseService.getHighlightCourses(currentUser.getId(), currentUser.getRole(), total);
  }

  @GetMapping("/search")
  public List<CourseResponse> searchCourses(
      @RequestParam(required = false) String subjectId,
      @RequestParam(required = false) Boolean enrolled,
      @RequestParam(required = false) String keyword,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);

    // Use the same optimized method as getAll - they have identical logic
    List<Course> filteredCourses = courseService.getCoursesOptimized(
        currentUser.getId(),
        currentUser.getRole(),
        subjectId,
        enrolled,
        keyword);

    return filteredCourses.stream()
        .map(courseService::toCourseResponse)
        .toList();
  }
}