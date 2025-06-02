package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.*;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.PostService;
import com.example.eduworldbe.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
  @Autowired
  private PostService postService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public ResponseEntity<PostDTO> createPost(@RequestBody CreatePostRequest request, HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    String userId = currentUser.getId();
    return ResponseEntity.ok(postService.createPost(request, userId));
  }

  @PutMapping("/{postId}")
  public ResponseEntity<PostDTO> updatePost(@PathVariable String postId, @RequestBody UpdatePostRequest request,
      HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    String userId = currentUser.getId();
    return ResponseEntity.ok(postService.updatePost(postId, request, userId));
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<Void> deletePost(@PathVariable String postId, HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    postService.deletePost(postId, currentUser.getId());
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{postId}/approve")
  public ResponseEntity<PostDTO> approvePost(@PathVariable String postId, @RequestBody ApprovePostRequest request,
      HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    String userId = currentUser.getId();
    return ResponseEntity.ok(postService.approvePost(postId, request, userId));
  }

  @GetMapping("/course/{courseId}")
  public ResponseEntity<PostPageResponse> getPostsByCourse(
      @PathVariable String courseId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(postService.getPostsByCourse(courseId, page, size));
  }

  @GetMapping("/course/{courseId}/pending")
  public ResponseEntity<PostPageResponse> getPendingPosts(
      @PathVariable String courseId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(postService.getPendingPosts(courseId, page, size));
  }
}