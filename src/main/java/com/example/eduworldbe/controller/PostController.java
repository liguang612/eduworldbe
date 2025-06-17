package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.*;
import com.example.eduworldbe.dto.request.ApprovePostRequest;
import com.example.eduworldbe.dto.request.CreateCommentRequest;
import com.example.eduworldbe.dto.request.CreatePostRequest;
import com.example.eduworldbe.dto.request.UpdateCommentRequest;
import com.example.eduworldbe.dto.request.UpdatePostRequest;
import com.example.eduworldbe.dto.response.CommentPageResponse;
import com.example.eduworldbe.dto.response.PostPageResponse;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.PostService;
import com.example.eduworldbe.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PostController {
  @Autowired
  private PostService postService;

  @Autowired
  private AuthUtil authUtil;

  // POST
  @PostMapping("/posts")
  public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostRequest request, HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    String userId = currentUser.getId();
    return ResponseEntity.ok(postService.createPost(request, userId));
  }

  @PutMapping("/posts/{postId}")
  public ResponseEntity<PostDTO> updatePost(@PathVariable String postId, @RequestBody UpdatePostRequest request,
      HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    String userId = currentUser.getId();
    return ResponseEntity.ok(postService.updatePost(postId, request, userId));
  }

  @DeleteMapping("/posts/{postId}")
  public ResponseEntity<Void> deletePost(@PathVariable String postId, HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    postService.deletePost(postId, currentUser.getId());
    return ResponseEntity.ok().build();
  }

  @PutMapping("/posts/{postId}/approve")
  public ResponseEntity<PostDTO> approvePost(@PathVariable String postId, @RequestBody ApprovePostRequest request,
      HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    return ResponseEntity.ok(postService.approvePost(postId, request, currentUser.getId()));
  }

  @GetMapping("/posts/course/{courseId}")
  public ResponseEntity<PostPageResponse> getPostsByCourse(
      @PathVariable String courseId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(postService.getPostsByCourse(courseId, page, size));
  }

  @GetMapping("/posts/course/{courseId}/pending")
  public ResponseEntity<PostPageResponse> getPendingPosts(
      @PathVariable String courseId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(postService.getPendingPosts(courseId, page, size));
  }

  // COMMENT
  @PostMapping("/comments")
  public ResponseEntity<CommentDTO> createComment(@RequestBody CreateCommentRequest request,
      HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    String userId = currentUser.getId();
    return ResponseEntity.ok(postService.createComment(request, userId));
  }

  @PutMapping("/comments/{commentId}")
  public ResponseEntity<CommentDTO> updateComment(@PathVariable String commentId,
      @RequestBody UpdateCommentRequest request, HttpServletRequest subRequest) {
    User currentUser = authUtil.getCurrentUser(subRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    return ResponseEntity.ok(postService.updateComment(commentId, request, currentUser.getId()));
  }

  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable String commentId, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    postService.deleteComment(commentId, currentUser.getId());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/comments/post/{postId}")
  public ResponseEntity<CommentPageResponse> getCommentsByPost(
      @PathVariable String postId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(postService.getCommentsByPost(postId, page, size));
  }
}