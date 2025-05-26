package com.example.eduworldbe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.eduworldbe.model.ReviewComment;
import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, String> {
  List<ReviewComment> findByReviewId(String reviewId);

  List<ReviewComment> findByUserId(String userId);
}