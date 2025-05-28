package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.*;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.CommentService;
import com.example.eduworldbe.util.AuthUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
  @Autowired
  private CommentService commentService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public ResponseEntity<CommentDTO> createComment(@RequestBody CreateCommentRequest request,
      HttpServletRequest servletRequest) {
    User currentUser = authUtil.getCurrentUser(servletRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    String userId = currentUser.getId();
    return ResponseEntity.ok(commentService.createComment(request, userId));
  }

  @PutMapping("/{commentId}")
  public ResponseEntity<CommentDTO> updateComment(@PathVariable String commentId,
      @RequestBody UpdateCommentRequest request, HttpServletRequest subRequest) {
    User currentUser = authUtil.getCurrentUser(subRequest);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    return ResponseEntity.ok(commentService.updateComment(commentId, request, currentUser.getId()));
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> deleteComment(@PathVariable String commentId, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }

    commentService.deleteComment(commentId, currentUser.getId());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/post/{postId}")
  public ResponseEntity<CommentPageResponse> getCommentsByPost(
      @PathVariable String postId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(commentService.getCommentsByPost(postId, page, size));
  }
}