package com.example.eduworldbe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.eduworldbe.model.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, String> {
  List<Review> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(Integer targetType, String targetId);

  Page<Review> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(Integer targetType, String targetId, Pageable pageable);

  List<Review> findByUserIdAndTargetTypeAndTargetIdOrderByCreatedAtDesc(String userId, Integer targetType,
      String targetId);

  List<Review> findByUserIdOrderByCreatedAtDesc(String userId);

  List<Review> findByTargetTypeAndTargetId(Integer targetType, String targetId);
}
