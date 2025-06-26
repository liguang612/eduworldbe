package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.response.ReviewCommentResponse;
import com.example.eduworldbe.dto.response.ReviewPageResponse;
import com.example.eduworldbe.dto.response.ReviewResponse;
import com.example.eduworldbe.dto.response.ReviewStatisticsResponse;
import com.example.eduworldbe.model.Review;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.model.ReviewComment;
import com.example.eduworldbe.service.ReviewService;
import com.example.eduworldbe.util.AuthUtil;

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
    User currentUser = authUtil.requireActiveUser(request);
    review.setUserId(currentUser.getId());
    return reviewService.create(review);
  }

  @GetMapping
  public List<ReviewResponse> getByTarget(@RequestParam Integer targetType, @RequestParam String targetId) {
    return reviewService.getByTarget(targetType, targetId);
  }

  @GetMapping("/page")
  public ReviewPageResponse getByTargetWithPagination(
      @RequestParam Integer targetType,
      @RequestParam String targetId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return reviewService.getByTargetWithPagination(targetType, targetId, page, size);
  }

  @GetMapping("/statistics")
  public ReviewStatisticsResponse getStatistics(
      @RequestParam Integer targetType,
      @RequestParam String targetId) {
    return reviewService.getStatistics(targetType, targetId);
  }

  @PostMapping("/{reviewId}/comments")
  public ReviewComment addComment(
      @PathVariable String reviewId,
      @RequestBody String content,
      HttpServletRequest request) {
    User currentUser = authUtil.requireActiveUser(request);
    return reviewService.addComment(reviewId, currentUser.getId(), content.substring(1, content.length() - 1));
  }

  @GetMapping("/{reviewId}/comments")
  public List<ReviewCommentResponse> getComments(@PathVariable String reviewId, HttpServletRequest request) {
    authUtil.requireActiveUser(request);
    return reviewService.getComments(reviewId);
  }

  @DeleteMapping("/comments/{commentId}")
  public void deleteComment(@PathVariable String commentId, HttpServletRequest request) {
    authUtil.requireActiveUser(request);
    reviewService.deleteComment(commentId);
  }

  @PutMapping("/{reviewId}")
  public Review update(@PathVariable String reviewId, @RequestBody Review review, HttpServletRequest request) {
    authUtil.requireActiveUser(request);
    return reviewService.update(reviewId, review);
  }
}