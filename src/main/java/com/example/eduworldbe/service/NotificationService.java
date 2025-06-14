package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.NotificationPageWithCursor;
import com.example.eduworldbe.dto.response.NotificationResponse;
import com.example.eduworldbe.model.*;
import com.example.eduworldbe.repository.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final UserRepository userRepository;
  private final CourseRepository courseRepository;
  private final LectureRepository lectureRepository;
  private final ExamRepository examRepository;
  private final PostRepository postRepository;
  private final QuestionRepository questionRepository;

  private static final String COLLECTION_NAME = "notifications";

  public void createNotification(Notification.NotificationBuilder builder)
      throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    if (builder.build().getCreatedAt() == null) {
      builder.createdAt(LocalDateTime.now());
    }
    builder.isRead(false);

    Notification notification = builder.build();

    // Convert LocalDateTime to Timestamp for Firestore
    LocalDateTime createdAt = notification.getCreatedAt();
    Timestamp timestamp = Timestamp.of(java.sql.Timestamp.valueOf(createdAt));

    DocumentReference docRef = db.collection(COLLECTION_NAME).document();
    notification.setId(docRef.getId());

    // Create a map of the notification data
    var data = new java.util.HashMap<String, Object>();
    data.put("id", notification.getId());
    data.put("userId", notification.getUserId());
    data.put("type", notification.getType());
    data.put("isRead", notification.isRead());
    data.put("createdAt", timestamp);
    data.put("actorId", notification.getActorId());
    data.put("courseId", notification.getCourseId());
    data.put("lectureId", notification.getLectureId());
    data.put("questionId", notification.getQuestionId());
    data.put("solutionId", notification.getSolutionId());
    data.put("examId", notification.getExamId());
    data.put("postId", notification.getPostId());
    data.put("commentId", notification.getCommentId());
    data.put("joinRequestId", notification.getJoinRequestId());

    // Fire and forget - don't wait for the result
    docRef.set(data);
  }

  public NotificationPageWithCursor getNotificationsForUser(String userId, String startAfterDocId, int pageSize)
      throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference notificationsCollection = db.collection(COLLECTION_NAME);

    Query query = notificationsCollection
        .whereEqualTo("userId", userId)
        .orderBy("createdAt", Query.Direction.DESCENDING);

    if (startAfterDocId != null && !startAfterDocId.isEmpty()) {
      DocumentSnapshot startAfterSnapshot = db.collection(COLLECTION_NAME).document(startAfterDocId).get().get();
      if (startAfterSnapshot.exists()) {
        query = query.startAfter(startAfterSnapshot);
      }
    }

    // Cứ lấy thừa thêm 1 notification để check xem có next page không
    ApiFuture<QuerySnapshot> querySnapshot = query.limit(pageSize + 1).get();
    List<Notification> notifications = new ArrayList<>();

    for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
      Notification notification = new Notification();
      notification.setId(document.getString("id"));
      notification.setUserId(document.getString("userId"));
      notification.setType(document.get("type", NotificationType.class));
      if (document.getBoolean("isRead") != null) {
        notification.setRead(document.getBoolean("isRead"));
      }

      // Convert Timestamp back to LocalDateTime
      Timestamp timestamp = document.getTimestamp("createdAt");
      if (timestamp != null) {
        notification.setCreatedAt(timestamp.toDate().toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime());
      }

      notification.setActorId(document.getString("actorId"));
      notification.setCourseId(document.getString("courseId"));
      notification.setLectureId(document.getString("lectureId"));
      notification.setQuestionId(document.getString("questionId"));
      notification.setSolutionId(document.getString("solutionId"));
      notification.setExamId(document.getString("examId"));
      notification.setPostId(document.getString("postId"));
      notification.setCommentId(document.getString("commentId"));
      notification.setJoinRequestId(document.getString("joinRequestId"));

      notifications.add(notification);
    }

    // Bỏ cái thừa đi
    boolean hasNextPage = notifications.size() > pageSize;
    List<Notification> currentPageNotifications = hasNextPage ? notifications.subList(0, pageSize) : notifications;

    List<NotificationResponse> responseList = currentPageNotifications.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());

    String nextCursor = null;
    if (hasNextPage && !currentPageNotifications.isEmpty()) {
      // next cursor là ID của notification cuối cùng trong *current page*
      // thực tế được lấy ra trong limit ban đầu
      nextCursor = currentPageNotifications.get(currentPageNotifications.size() - 1).getId();
    }

    return NotificationPageWithCursor.builder()
        .notifications(responseList)
        .nextCursor(nextCursor)
        .hasNextPage(hasNextPage)
        .build();
  }

  private NotificationResponse mapToResponse(Notification notification) {
    NotificationResponse.NotificationResponseBuilder builder = NotificationResponse.builder()
        .id(notification.getId())
        .type(notification.getType())
        .isRead(notification.isRead())
        .createdAt(notification.getCreatedAt())
        .actorId(notification.getActorId())
        .courseId(notification.getCourseId())
        .lectureId(notification.getLectureId())
        .questionId(notification.getQuestionId())
        .solutionId(notification.getSolutionId())
        .examId(notification.getExamId())
        .postId(notification.getPostId())
        .commentId(notification.getCommentId())
        .joinRequestId(notification.getJoinRequestId());

    if (notification.getActorId() != null) {
      userRepository.findById(notification.getActorId()).ifPresent(actor -> {
        builder.actorName(actor.getName());
        builder.actorAvatarUrl(actor.getAvatar());
      });
    }
    if (notification.getCourseId() != null) {
      courseRepository.findById(notification.getCourseId()).ifPresent(course -> {
        builder.courseName(course.getName());
        builder.courseAvatarUrl(course.getAvatar());
      });
    }
    if (notification.getLectureId() != null) {
      lectureRepository.findById(notification.getLectureId()).ifPresent(lecture -> {
        builder.lectureTitle(lecture.getName());
      });
    }
    if (notification.getExamId() != null) {
      examRepository.findById(notification.getExamId()).ifPresent(exam -> {
        builder.examTitle(exam.getTitle());
      });
    }
    if (notification.getPostId() != null) {
      postRepository.findById(notification.getPostId()).ifPresent(post -> {
        builder.postTitle(post.getContent().substring(0, Math.min(post.getContent().length(), 50)) + "...");
      });
    }
    if (notification.getQuestionId() != null) {
      questionRepository.findById(notification.getQuestionId()).ifPresent(question -> {
        builder.questionTitle(question.getTitle());
      });
    }

    builder.message(buildMessage(notification, builder.build()));
    return builder.build();
  }

  private String buildMessage(Notification notification, NotificationResponse dto) {
    String actorName = dto.getActorName() != null ? dto.getActorName() : "Someone";
    String courseName = dto.getCourseName() != null ? dto.getCourseName() : "a course";
    String lectureTitle = dto.getLectureTitle() != null ? dto.getLectureTitle() : "a new lecture";
    String examTitle = dto.getExamTitle() != null ? dto.getExamTitle() : "a new exam";
    String questionTitle = dto.getQuestionTitle() != null ? dto.getQuestionTitle() : "a question";

    switch (notification.getType()) {
      case STUDENT_ADDED_TO_COURSE:
        return String.format("You have been added to the course: %s.", courseName);
      case JOIN_REQUEST_ACCEPTED:
        return String.format("Your request to join course '%s' has been accepted.", courseName);
      case JOIN_REQUEST_REJECTED:
        return String.format("Your request to join course '%s' has been rejected.", courseName);
      case SOLUTION_ACCEPTED:
        return String.format("Your solution for question '%s' has been accepted.", questionTitle);
      case SOLUTION_REJECTED:
        return String.format("Your solution for question '%s' has been rejected.", questionTitle);
      case POST_APPROVED:
        return String.format("Your post in course '%s' has been approved.", courseName);
      case POST_REJECTED:
        return String.format("Your post in course '%s' has been rejected.", courseName);
      case COMMENT_ON_OWN_POST:
        return String.format("%s commented on your post in course '%s'.", actorName, courseName);
      case NEW_LECTURE_IN_COURSE:
        return String.format("A new lecture '%s' has been added to course '%s'.", lectureTitle, courseName);
      case NEW_EXAM_IN_COURSE:
        return String.format("A new exam '%s' has been created in course '%s'.", examTitle, courseName);
      case NEW_JOIN_REQUEST_FOR_TEACHER:
        return String.format("%s has requested to join your course '%s'.", actorName, courseName);
      case NEW_SOLUTION_FOR_TEACHER_APPROVAL:
        return String.format("%s submitted a new solution for question '%s' in course '%s'.", actorName, questionTitle,
            courseName);
      case NEW_POST_FOR_TEACHER_APPROVAL:
        return String.format("%s created a new post in course '%s' awaiting approval.", actorName, courseName);
      default:
        return "You have a new notification.";
    }
  }

  public void markNotificationAsRead(String notificationId, String userId)
      throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    DocumentReference docRef = db.collection(COLLECTION_NAME).document(notificationId);
    ApiFuture<DocumentSnapshot> future = docRef.get();
    DocumentSnapshot document = future.get();
    if (document.exists()) {
      String docUserId = document.getString("userId");

      if (docUserId != null && docUserId.equals(userId)) {
        docRef.update("isRead", true);
      } else if (docUserId == null) {
        System.err.println("userId field is missing for notification ID: " + notificationId);
      } else {
        System.err.println("User mismatch for notification ID: " + notificationId + ". Expected userId: " + userId
            + ", found: " + docUserId);
      }
    } else {
      System.err.println("Notification not found in Firestore: " + notificationId);
      throw new RuntimeException("Notification not found: " + notificationId);
    }
  }

  public void markAllNotificationsAsRead(String userId) throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    WriteBatch batch = db.batch();
    Query query = db.collection(COLLECTION_NAME)
        .whereEqualTo("userId", userId)
        .whereEqualTo("isRead", false);

    ApiFuture<QuerySnapshot> future = query.get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();

    if (!documents.isEmpty()) {
      for (DocumentSnapshot document : documents) {
        batch.update(document.getReference(), "isRead", true);
      }
      ApiFuture<List<WriteResult>> batchResult = batch.commit();
      batchResult.get();
    }
  }

  public void deleteAllNotificationsForUser(String userId) throws ExecutionException, InterruptedException {
    Firestore db = FirestoreClient.getFirestore();
    WriteBatch batch = db.batch();
    Query query = db.collection(COLLECTION_NAME).whereEqualTo("userId", userId);

    ApiFuture<QuerySnapshot> future = query.get();
    List<QueryDocumentSnapshot> documents = future.get().getDocuments();

    if (!documents.isEmpty()) {
      for (DocumentSnapshot document : documents) {
        batch.delete(document.getReference());
      }
      ApiFuture<List<WriteResult>> batchResult = batch.commit();
      batchResult.get();
    }
  }
}