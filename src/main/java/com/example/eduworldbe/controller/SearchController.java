package com.example.eduworldbe.controller;

import com.example.eduworldbe.service.CourseService;
import com.example.eduworldbe.service.LectureService;
import com.example.eduworldbe.service.ExamService;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.dto.response.CourseResponse;
import com.example.eduworldbe.dto.response.ExamResponse;
import com.example.eduworldbe.dto.response.LectureResponse;
import com.example.eduworldbe.model.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/search")
public class SearchController {
  @Autowired
  private CourseService courseService;

  @Autowired
  private LectureService lectureService;

  @Autowired
  private ExamService examService;

  @Autowired
  private AuthUtil authUtil;

  @GetMapping
  public ResponseEntity<?> search(
      @RequestParam String type,
      @RequestParam(required = false) String subjectId,
      @RequestParam(required = false) String grade,
      @RequestParam(required = false, defaultValue = "name") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortOrder,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size,
      HttpServletRequest request) {

    User currentUser = authUtil.requireActiveUser(request);

    switch (type.toLowerCase()) {
      case "course":
        List<CourseResponse> courseResponse = courseService.searchCourses(
            currentUser.getId(),
            currentUser.getRole(),
            subjectId,
            grade,
            sortBy,
            sortOrder,
            keyword);
        return ResponseEntity.ok(courseResponse);

      case "lecture":
        List<LectureResponse> lectureResponse = lectureService.searchLectures(
            currentUser.getId(),
            currentUser.getRole(),
            subjectId,
            grade,
            sortBy,
            sortOrder,
            keyword);
        return ResponseEntity.ok(lectureResponse);

      case "exam":
        List<ExamResponse> examResponse = examService.searchExams(
            currentUser.getId(),
            currentUser.getRole(),
            subjectId,
            grade,
            sortBy,
            sortOrder,
            keyword);
        return ResponseEntity.ok(examResponse);

      default:
        return ResponseEntity.badRequest().body("Invalid type. Must be one of: course, lecture, exam");
    }
  }
}