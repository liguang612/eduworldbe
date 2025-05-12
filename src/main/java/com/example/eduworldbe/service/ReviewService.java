package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Review;
import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.repository.ReviewRepository;
import com.example.eduworldbe.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service
public class ReviewService {
  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private CourseRepository courseRepository;

  public Review create(Review review) {
    review.setCreatedAt(new Date());
    Review savedReview = reviewRepository.save(review);

    if (review.getTargetType() == 1) {
      Course course = courseRepository.findById(review.getTargetId()).orElse(null);
      if (course != null) {
        if (course.getReviewIds() == null) {
          course.setReviewIds(new ArrayList<>());
        }
        course.getReviewIds().add(savedReview.getId());
        courseRepository.save(course);
      }
    }
    // TODO: lecture, question, exam

    return savedReview;
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