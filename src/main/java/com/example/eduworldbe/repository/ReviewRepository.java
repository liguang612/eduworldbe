package com.example.eduworldbe.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.eduworldbe.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
  List<Review> findByTargetTypeAndTargetId(Integer targetType, String targetId);

  List<Review> findByUserIdAndTargetTypeAndTargetId(String userId, Integer targetType, String targetId);

  List<Review> findByUserId(String userId);
}
