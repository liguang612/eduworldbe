package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Review;
import com.example.eduworldbe.model.User;
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
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new RuntimeException("Unauthorized");
    }
    review.setUserId(currentUser.getId());
    return reviewService.create(review);
  }

  @GetMapping
  public List<Review> getByTarget(@RequestParam Integer targetType, @RequestParam String targetId) {
    return reviewService.getByTarget(targetType, targetId);
  }

  @GetMapping("/average")
  public double getAverageScore(@RequestParam Integer targetType, @RequestParam String targetId) {
    return reviewService.getAverageScore(targetType, targetId);
  }
}