package com.example.eduworldbe.service;

import com.example.eduworldbe.model.CourseJoinRequest;
import com.example.eduworldbe.repository.CourseJoinRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CourseJoinRequestService {
  @Autowired
  private CourseJoinRequestRepository repository;

  public CourseJoinRequest create(String courseId, String userId, Integer role) {
    CourseJoinRequest req = new CourseJoinRequest();
    req.setCourseId(courseId);
    req.setUserId(userId);
    req.setRole(role);
    req.setStatus(0); // pending
    req.setCreatedAt(new Date());
    return repository.save(req);
  }

  public List<CourseJoinRequest> getByCourseAndStatus(String courseId, Integer status) {
    return repository.findByCourseIdAndStatus(courseId, status);
  }

  public List<CourseJoinRequest> getByUser(String userId) {
    return repository.findByUserId(userId);
  }

  public Optional<CourseJoinRequest> getById(String id) {
    return repository.findById(id);
  }

  public CourseJoinRequest approve(String id) {
    CourseJoinRequest req = repository.findById(id).orElseThrow();
    req.setStatus(1);
    return repository.save(req);
  }

  public CourseJoinRequest reject(String id) {
    CourseJoinRequest req = repository.findById(id).orElseThrow();
    req.setStatus(2);
    return repository.save(req);
  }
}
