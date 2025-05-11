package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Review;
import com.example.eduworldbe.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReviewService {
  @Autowired
  private ReviewRepository reviewRepository;

  public Review create(Review review) {
    review.setCreatedAt(new Date());
    return reviewRepository.save(review);
  }

  public List<Review> getByTarget(Integer targetType, String targetId) {
    return reviewRepository.findByTargetTypeAndTargetId(targetType, targetId);
  }

  public double getAverageScore(Integer targetType, String targetId) {
    List<Review> reviews = getByTarget(targetType, targetId);
    if (reviews.isEmpty())
      return 0;
    return reviews.stream().mapToInt(Review::getScore).average().orElse(0);
  }

  public int getReviewCount(Integer targetType, String targetId) {
    return getByTarget(targetType, targetId).size();
  }
}