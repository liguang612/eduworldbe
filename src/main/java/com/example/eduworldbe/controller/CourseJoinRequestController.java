package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.CourseJoinRequest;
import com.example.eduworldbe.service.CourseJoinRequestService;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}/join-requests")
public class CourseJoinRequestController {
  @Autowired
  private CourseJoinRequestService service;

  @Autowired
  private AuthUtil authUtil;

  // Học sinh/trợ giảng gửi yêu cầu tham gia lớp
  @PostMapping
  public CourseJoinRequest create(@PathVariable String courseId, @RequestBody JoinRequestBody body,
      HttpServletRequest request) {
    User user = authUtil.getCurrentUser(request);
    if (user == null)
      throw new RuntimeException("Unauthorized");
    return service.create(courseId, user.getId(), body.getRole());
  }

  // Giáo viên lấy danh sách yêu cầu vào lớp (theo status)
  @GetMapping
  public List<CourseJoinRequest> getByCourseAndStatus(@PathVariable String courseId, @RequestParam Integer status) {
    return service.getByCourseAndStatus(courseId, status);
  }

  // Giáo viên phê duyệt yêu cầu
  @PutMapping("/{requestId}/approve")
  public CourseJoinRequest approve(@PathVariable String courseId, @PathVariable String requestId,
      HttpServletRequest request) {
    User user = authUtil.getCurrentUser(request);
    if (user == null)
      throw new RuntimeException("Unauthorized");
    return service.approve(requestId, user.getId());
  }

  // Giáo viên từ chối yêu cầu
  @PutMapping("/{requestId}/reject")
  public CourseJoinRequest reject(@PathVariable String courseId, @PathVariable String requestId,
      HttpServletRequest request) {
    User user = authUtil.getCurrentUser(request);
    if (user == null)
      throw new RuntimeException("Unauthorized");
    return service.reject(requestId, user.getId());
  }
}

// DTO cho body gửi yêu cầu
class JoinRequestBody {
  private Integer role;

  public Integer getRole() {
    return role;
  }

  public void setRole(Integer role) {
    this.role = role;
  }
}
