package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.*;
import com.example.eduworldbe.dto.request.ApprovePostRequest;
import com.example.eduworldbe.dto.request.CreateCommentRequest;
import com.example.eduworldbe.dto.request.CreatePostRequest;
import com.example.eduworldbe.dto.request.UpdateCommentRequest;
import com.example.eduworldbe.dto.request.UpdatePostRequest;
import com.example.eduworldbe.dto.response.CommentPageResponse;
import com.example.eduworldbe.dto.response.PostPageResponse;
import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.model.Post;
import com.example.eduworldbe.model.Comment;
import com.example.eduworldbe.repository.CourseRepository;
import com.example.eduworldbe.repository.PostRepository;
import com.example.eduworldbe.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;

import com.example.eduworldbe.model.Notification;
import com.example.eduworldbe.model.NotificationType;
import java.util.concurrent.ExecutionException;

@Service
public class PostService {
  @Autowired
  private PostRepository postRepository;

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private FileUploadService fileUploadService;

  @Autowired
  private NotificationService notificationService;

  @Autowired
  private WebSocketNotificationService webSocketNotificationService;

  // POST

  @Transactional
  public PostDTO createPost(CreatePostRequest request, String userId) {
    Course course = courseRepository.findById(request.getCourseId())
        .orElseThrow(() -> new RuntimeException("Course not found"));

    boolean isTeacher = userId.equals(course.getTeacherId());
    boolean isTeacherAssistant = course.getTeacherAssistantIds() != null
        && course.getTeacherAssistantIds().contains(userId);
    boolean isStudent = course.getStudentIds() != null && course.getStudentIds().contains(userId);

    if (!isTeacher && !isTeacherAssistant && !isStudent) {
      throw new RuntimeException("Not authorized to post in this course");
    }

    if (isStudent && !course.isAllowStudentPost()) {
      throw new RuntimeException("Students are not allowed to post in this course");
    }

    Post post = new Post();
    post.setContent(request.getContent());
    post.setImageUrls(request.getImageUrls());
    post.setCreatedAt(LocalDateTime.now());
    post.setUserId(userId);
    post.setCourseId(request.getCourseId());

    boolean autoApprove = !course.isRequirePostApproval() || isTeacher || isTeacherAssistant;
    post.setApproved(autoApprove);

    Post savedPost = postRepository.save(post);

    if (!autoApprove) {
      List<String> recipients = new ArrayList<>();
      if (course.getTeacherId() != null) {
        recipients.add(course.getTeacherId());
      }
      if (course.getTeacherAssistantIds() != null) {
        recipients.addAll(course.getTeacherAssistantIds());
      }

      recipients.stream().distinct().filter(id -> !id.equals(userId)).forEach(recipientId -> {
        try {
          Notification.NotificationBuilder notification = Notification.builder()
              .userId(recipientId)
              .type(NotificationType.NEW_POST_FOR_TEACHER_APPROVAL)
              .actorId(userId)
              .postId(savedPost.getId())
              .courseId(savedPost.getCourseId());

          notificationService.createNotification(notification);

          webSocketNotificationService.sendNotificationToUser(recipientId, notification.build());
        } catch (ExecutionException | InterruptedException e) {
          System.err.println("Failed to create NEW_POST_FOR_TEACHER_APPROVAL notification for user " + recipientId
              + ": " + e.getMessage());
          Thread.currentThread().interrupt();
        }
      });
    }

    return convertToDTO(savedPost);
  }

  @Transactional
  public PostDTO updatePost(String postId, UpdatePostRequest request, String userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));

    if (!post.getUserId().equals(userId)) {
      throw new RuntimeException("Not authorized to update this post");
    }

    // Xóa những ảnh không còn được sử dụng
    fileUploadService.deleteUnusedFiles(post.getImageUrls(), request.getImageUrls());

    post.setContent(request.getContent());
    post.setImageUrls(request.getImageUrls());

    Post updatedPost = postRepository.save(post);
    return convertToDTO(updatedPost);
  }

  @Transactional
  public void deletePost(String postId, String userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));

    Course course = courseRepository.findById(post.getCourseId())
        .orElseThrow(() -> new RuntimeException("Course not found"));

    boolean isTeacher = userId.equals(course.getTeacherId());
    boolean isTeacherAssistant = course.getTeacherAssistantIds().contains(userId);
    boolean isPostOwner = post.getUserId().equals(userId);

    if (!isTeacher && !isTeacherAssistant && !isPostOwner) {
      throw new RuntimeException("Not authorized to delete this post");
    }

    // Xóa tất cả các ảnh của post
    fileUploadService.deleteFiles(post.getImageUrls());

    postRepository.delete(post);
  }

  @Transactional
  public PostDTO approvePost(String postId, ApprovePostRequest request, String userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("Post not found"));

    if (request.isApproved()) {
      post.setApproved(true);
      Post updatedPost = postRepository.save(post);

      try {
        Notification.NotificationBuilder notification = Notification.builder()
            .userId(post.getUserId())
            .type(NotificationType.POST_APPROVED)
            .actorId(userId)
            .postId(postId)
            .courseId(post.getCourseId());

        notificationService.createNotification(notification);

        webSocketNotificationService.sendNotificationToUser(post.getUserId(), notification.build());
      } catch (ExecutionException | InterruptedException e) {
        System.err.println(
            "Failed to create POST_APPROVED notification for user " + post.getUserId() + ": " + e.getMessage());
        Thread.currentThread().interrupt();
      }

      return convertToDTO(updatedPost);
    } else {
      try {
        Notification.NotificationBuilder notification = Notification.builder()
            .userId(post.getUserId())
            .type(NotificationType.POST_REJECTED)
            .actorId(userId)
            .postId(postId)
            .courseId(post.getCourseId());

        notificationService.createNotification(notification);

        webSocketNotificationService.sendNotificationToUser(post.getUserId(), notification.build());
      } catch (ExecutionException | InterruptedException e) {
        System.err.println(
            "Failed to create POST_REJECTED notification for user " + post.getUserId() + ": " + e.getMessage());
        Thread.currentThread().interrupt();
      }
      deletePost(postId, userId);
      return null;
    }
  }

  public PostPageResponse getPostsByCourse(String courseId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Post> postPage = postRepository.findByCourseIdAndApprovedOrderByCreatedAtDesc(courseId, true, pageable);

    PostPageResponse response = new PostPageResponse();
    response.setPosts(postPage.getContent().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList()));
    response.setCurrentPage(postPage.getNumber());
    response.setTotalPages(postPage.getTotalPages());
    response.setTotalElements(postPage.getTotalElements());
    response.setPageSize(postPage.getSize());

    return response;
  }

  public PostPageResponse getPendingPosts(String courseId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Post> postPage = postRepository.findByCourseIdAndApprovedOrderByCreatedAtDesc(courseId, false, pageable);

    PostPageResponse response = new PostPageResponse();
    response.setPosts(postPage.getContent().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList()));
    response.setCurrentPage(postPage.getNumber());
    response.setTotalPages(postPage.getTotalPages());
    response.setTotalElements(postPage.getTotalElements());
    response.setPageSize(postPage.getSize());

    return response;
  }

  // COMMENT
  @Transactional
  public CommentDTO createComment(CreateCommentRequest request, String userId) {
    Post post = postRepository.findById(request.getPostId())
        .orElseThrow(() -> new RuntimeException("Post not found"));

    if (!post.isApproved()) {
      throw new RuntimeException("Cannot comment on unapproved post");
    }

    Comment comment = new Comment();
    comment.setPostId(request.getPostId());
    comment.setUserId(userId);
    comment.setCreatedAt(LocalDateTime.now());
    comment.setContent(request.getContent());

    Comment savedComment = commentRepository.save(comment);

    String postOwnerId = post.getUserId();
    if (postOwnerId != null && !postOwnerId.equals(userId)) {
      try {
        Notification.NotificationBuilder notification = Notification.builder()
            .userId(postOwnerId)
            .type(NotificationType.COMMENT_ON_OWN_POST)
            .actorId(userId)
            .postId(post.getId())
            .commentId(savedComment.getId())
            .courseId(post.getCourseId());

        notificationService.createNotification(notification);

        webSocketNotificationService.sendNotificationToUser(postOwnerId, notification.build());
      } catch (ExecutionException | InterruptedException e) {
        System.err.println(
            "Failed to create COMMENT_ON_OWN_POST notification for user " + postOwnerId + ": " + e.getMessage());
        Thread.currentThread().interrupt();
      }
    }

    return convertCommentToDTO(savedComment);
  }

  @Transactional
  public CommentDTO updateComment(String commentId, UpdateCommentRequest request, String userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Comment not found"));

    if (!comment.getUserId().equals(userId)) {
      throw new RuntimeException("Not authorized to update this comment");
    }

    comment.setContent(request.getContent());
    Comment updatedComment = commentRepository.save(comment);
    return convertCommentToDTO(updatedComment);
  }

  @Transactional
  public void deleteComment(String commentId, String userId) {
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("Comment not found"));

    if (!comment.getUserId().equals(userId)) {
      throw new RuntimeException("Not authorized to delete this comment");
    }

    commentRepository.delete(comment);
  }

  public CommentPageResponse getCommentsByPost(String postId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Comment> commentPage = commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);

    CommentPageResponse response = new CommentPageResponse();
    response.setComments(commentPage.getContent().stream()
        .map(this::convertCommentToDTO)
        .collect(Collectors.toList()));
    response.setCurrentPage(commentPage.getNumber());
    response.setTotalPages(commentPage.getTotalPages());
    response.setTotalElements(commentPage.getTotalElements());
    response.setPageSize(commentPage.getSize());

    return response;
  }

  private PostDTO convertToDTO(Post post) {
    PostDTO dto = new PostDTO();
    dto.setId(post.getId());
    dto.setContent(post.getContent());
    dto.setImageUrls(post.getImageUrls());
    dto.setCreatedAt(post.getCreatedAt());
    dto.setUser(userService.getUserInfo(post.getUserId()));
    dto.setCourseId(post.getCourseId());
    dto.setApproved(post.isApproved());
    return dto;
  }

  private CommentDTO convertCommentToDTO(Comment comment) {
    CommentDTO dto = new CommentDTO();
    dto.setId(comment.getId());
    dto.setPostId(comment.getPostId());
    dto.setUser(userService.getUserInfo(comment.getUserId()));
    dto.setCreatedAt(comment.getCreatedAt());
    dto.setContent(comment.getContent());
    return dto;
  }
}