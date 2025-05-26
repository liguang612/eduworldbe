package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Review;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.model.ReviewComment;
import com.example.eduworldbe.dto.ReviewResponse;
import com.example.eduworldbe.dto.ReviewCommentResponse;
import com.example.eduworldbe.service.ReviewService;
import com.example.eduworldbe.util.AuthUtil;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
  @Autowired
  private ReviewService reviewService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public Review create(@RequestBody Review review, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }
    review.setUserId(currentUser.getId());
    return reviewService.create(review);
  }

  @GetMapping
  public List<ReviewResponse> getByTarget(@RequestParam Integer targetType, @RequestParam String targetId) {
    return reviewService.getByTarget(targetType, targetId);
  }

  @GetMapping("/average")
  public double getAverageScore(@RequestParam Integer targetType, @RequestParam String targetId) {
    return reviewService.getAverageScore(targetType, targetId);
  }

  @PostMapping("/{reviewId}/comments")
  public ReviewComment addComment(
      @PathVariable String reviewId,
      @RequestBody String content,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }
    return reviewService.addComment(reviewId, currentUser.getId(), content);
  }

  @GetMapping("/{reviewId}/comments")
  public List<ReviewCommentResponse> getComments(@PathVariable String reviewId) {
    return reviewService.getComments(reviewId);
  }

  @DeleteMapping("/comments/{commentId}")
  public void deleteComment(@PathVariable String commentId, HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }
    reviewService.deleteComment(commentId);
  }
}