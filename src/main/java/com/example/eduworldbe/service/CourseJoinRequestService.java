package com.example.eduworldbe.service;

import com.example.eduworldbe.model.CourseJoinRequest;
import com.example.eduworldbe.model.Notification;
import com.example.eduworldbe.model.NotificationType;
import com.example.eduworldbe.repository.CourseJoinRequestRepository;
import com.example.eduworldbe.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Service
public class CourseJoinRequestService {
  @Autowired
  private CourseJoinRequestRepository repository;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private CourseRepository courseRepository;

  public CourseJoinRequest create(String courseId, String userId, Integer role) {
    CourseJoinRequest req = new CourseJoinRequest();
    req.setCourseId(courseId);
    req.setUserId(userId);
    req.setRole(role);
    req.setStatus(0); // pending
    req.setCreatedAt(new Date());
    CourseJoinRequest savedRequest = repository.save(req);

    // Notify teacher and TAs about the new join request
    courseRepository.findById(courseId).ifPresent(course -> {
      List<String> recipients = new ArrayList<>();
      if (course.getTeacherId() != null) {
        recipients.add(course.getTeacherId());
      }
      if (course.getTeacherAssistantIds() != null) {
        recipients.addAll(course.getTeacherAssistantIds());
      }

      for (String recipientId : recipients) {
        // Ensure not notifying the requester themselves if they somehow are a
        // TA/teacher
        if (recipientId != null && !recipientId.equals(userId)) {
          try {
            notificationService.createNotification(Notification.builder()
                .userId(recipientId) // Teacher or TA
                .type(NotificationType.NEW_JOIN_REQUEST_FOR_TEACHER)
                .actorId(userId) // Student who made the request
                .courseId(courseId)
                .joinRequestId(savedRequest.getId()));
          } catch (ExecutionException | InterruptedException e) {
            System.err.println("Failed to create NEW_JOIN_REQUEST_FOR_TEACHER notification for user " + recipientId
                + ": " + e.getMessage());
            Thread.currentThread().interrupt();
          }
        }
      }
    });

    return savedRequest;
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

  public CourseJoinRequest approve(String id, String actorId) {
    CourseJoinRequest req = repository.findById(id).orElseThrow(() -> new RuntimeException("Join request not found"));
    req.setStatus(1); // approved
    CourseJoinRequest savedRequest = repository.save(req);

    try {
      notificationService.createNotification(Notification.builder()
          .userId(savedRequest.getUserId()) // Student who made the request
          .type(NotificationType.JOIN_REQUEST_ACCEPTED)
          .actorId(actorId)
          .courseId(savedRequest.getCourseId())
          .joinRequestId(savedRequest.getId()));
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Failed to create JOIN_REQUEST_ACCEPTED notification for user " + savedRequest.getUserId()
          + ": " + e.getMessage());
      Thread.currentThread().interrupt();
    }
    return savedRequest;
  }

  public CourseJoinRequest reject(String id, String actorId) {
    CourseJoinRequest req = repository.findById(id).orElseThrow(() -> new RuntimeException("Join request not found"));
    req.setStatus(2); // rejected
    CourseJoinRequest savedRequest = repository.save(req);

    try {
      notificationService.createNotification(Notification.builder()
          .userId(savedRequest.getUserId()) // Student who made the request
          .type(NotificationType.JOIN_REQUEST_REJECTED)
          .actorId(actorId)
          .courseId(savedRequest.getCourseId())
          .joinRequestId(savedRequest.getId()));
    } catch (ExecutionException | InterruptedException e) {
      System.err.println("Failed to create JOIN_REQUEST_REJECTED notification for user " + savedRequest.getUserId()
          + ": " + e.getMessage());
      Thread.currentThread().interrupt();
    }
    return savedRequest;
  }
}
