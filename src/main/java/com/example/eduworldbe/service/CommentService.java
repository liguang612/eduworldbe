package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.*;
import com.example.eduworldbe.model.Comment;
import com.example.eduworldbe.model.Post;
import com.example.eduworldbe.repository.CommentRepository;
import com.example.eduworldbe.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.example.eduworldbe.model.Notification;
import com.example.eduworldbe.model.NotificationType;
import java.util.concurrent.ExecutionException;

@Service
public class CommentService {
  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private NotificationService notificationService;

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
        notificationService.createNotification(Notification.builder()
            .userId(postOwnerId)
            .type(NotificationType.COMMENT_ON_OWN_POST)
            .actorId(userId)
            .postId(post.getId())
            .commentId(savedComment.getId())
            .courseId(post.getCourseId()));
      } catch (ExecutionException | InterruptedException e) {
        System.err.println(
            "Failed to create COMMENT_ON_OWN_POST notification for user " + postOwnerId + ": " + e.getMessage());
        Thread.currentThread().interrupt();
      }
    }

    return convertToDTO(savedComment);
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
    return convertToDTO(updatedComment);
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
        .map(this::convertToDTO)
        .collect(Collectors.toList()));
    response.setCurrentPage(commentPage.getNumber());
    response.setTotalPages(commentPage.getTotalPages());
    response.setTotalElements(commentPage.getTotalElements());
    response.setPageSize(commentPage.getSize());

    return response;
  }

  private CommentDTO convertToDTO(Comment comment) {
    CommentDTO dto = new CommentDTO();
    dto.setId(comment.getId());
    dto.setPostId(comment.getPostId());
    dto.setUser(userService.getUserInfo(comment.getUserId()));
    dto.setCreatedAt(comment.getCreatedAt());
    dto.setContent(comment.getContent());
    return dto;
  }
}