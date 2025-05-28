package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Review;
import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.model.Exam;
import com.example.eduworldbe.model.Lecture;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.model.ReviewComment;
import com.example.eduworldbe.dto.ReviewResponse;
import com.example.eduworldbe.dto.ReviewCommentResponse;
import com.example.eduworldbe.dto.ReviewPageResponse;
import com.example.eduworldbe.dto.ReviewStatisticsResponse;
import com.example.eduworldbe.repository.ReviewRepository;
import com.example.eduworldbe.repository.CourseRepository;
import com.example.eduworldbe.repository.ExamRepository;
import com.example.eduworldbe.repository.LectureRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.repository.ReviewCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {
  @Autowired
  private ReviewRepository reviewRepository;

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private LectureRepository lectureRepository;

  @Autowired
  private ExamRepository examRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private ReviewCommentRepository reviewCommentRepository;

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
    } else if (review.getTargetType() == 2) {
      Lecture lecture = lectureRepository.findById(review.getTargetId()).orElse(null);
      if (lecture != null) {
        if (lecture.getReviewIds() == null) {
          lecture.setReviewIds(new ArrayList<>());
        }
      }
    } else if (review.getTargetType() == 4) {
      Exam exam = examRepository.findById(review.getTargetId()).orElse(null);
      if (exam != null) {
        if (exam.getReviewIds() == null) {
          exam.setReviewIds(new ArrayList<>());
        }
      }
    }

    return savedReview;
  }

  public Review update(String reviewId, Review review) {
    Review existingReview = reviewRepository.findById(reviewId).orElse(null);
    if (existingReview == null) {
      throw new RuntimeException("Review not found");
    }
    if (review.getScore() != 0) {
      existingReview.setScore(review.getScore());
    }
    if (review.getComment() != null) {
      existingReview.setComment(review.getComment());
    }
    return reviewRepository.save(existingReview);
  }

  public List<ReviewResponse> getByTarget(Integer targetType, String targetId) {
    List<Review> reviews = reviewRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
    return reviews.stream()
        .map(review -> {
          User user = userRepository.findById(review.getUserId()).orElse(null);
          List<ReviewComment> comments = reviewCommentRepository.findByReviewId(review.getId());
          List<ReviewCommentResponse> commentResponses = comments.stream()
              .map(comment -> {
                User commentUser = userRepository.findById(comment.getUserId()).orElse(null);
                return ReviewCommentResponse.fromCommentAndUser(comment, commentUser);
              })
              .collect(Collectors.toList());
          return ReviewResponse.fromReviewAndUser(review, user, commentResponses);
        })
        .collect(Collectors.toList());
  }

  public double getAverageScore(Integer targetType, String targetId) {
    List<ReviewResponse> reviews = getByTarget(targetType, targetId);
    if (reviews.isEmpty())
      return 0;
    return reviews.stream().mapToInt(ReviewResponse::getScore).average().orElse(0);
  }

  public int getReviewCount(Integer targetType, String targetId) {
    return getByTarget(targetType, targetId).size();
  }

  public ReviewComment addComment(String reviewId, String userId, String content) {
    ReviewComment comment = new ReviewComment();
    comment.setReviewId(reviewId);
    comment.setUserId(userId);
    comment.setContent(content);
    return reviewCommentRepository.save(comment);
  }

  public List<ReviewCommentResponse> getComments(String reviewId) {
    List<ReviewComment> comments = reviewCommentRepository.findByReviewId(reviewId);
    return comments.stream()
        .map(comment -> {
          User user = userRepository.findById(comment.getUserId()).orElse(null);
          return ReviewCommentResponse.fromCommentAndUser(comment, user);
        })
        .collect(Collectors.toList());
  }

  public void deleteComment(String commentId) {
    reviewCommentRepository.deleteById(commentId);
  }

  public ReviewPageResponse getByTargetWithPagination(Integer targetType, String targetId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<Review> reviewPage = reviewRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId,
        pageable);

    List<ReviewResponse> reviewResponses = reviewPage.getContent().stream()
        .map(review -> {
          User user = userRepository.findById(review.getUserId()).orElse(null);
          return ReviewResponse.fromReviewAndUser(review, user, new ArrayList<>());
        })
        .collect(Collectors.toList());

    ReviewPageResponse response = new ReviewPageResponse();
    response.setReviews(reviewResponses);
    response.setCurrentPage(page);
    response.setTotalPages(reviewPage.getTotalPages());
    response.setTotalElements(reviewPage.getTotalElements());

    return response;
  }

  public ReviewStatisticsResponse getStatistics(Integer targetType, String targetId) {
    List<Review> reviews = reviewRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);

    double averageScore = reviews.stream()
        .mapToInt(Review::getScore)
        .average()
        .orElse(0.0);

    Map<Integer, Long> scoreDistribution = reviews.stream()
        .collect(Collectors.groupingBy(
            Review::getScore,
            Collectors.counting()));

    for (int i = 1; i <= 5; i++) {
      scoreDistribution.putIfAbsent(i, 0L);
    }

    ReviewStatisticsResponse response = new ReviewStatisticsResponse();
    response.setAverageScore(averageScore);
    response.setScoreDistribution(scoreDistribution);

    return response;
  }
}