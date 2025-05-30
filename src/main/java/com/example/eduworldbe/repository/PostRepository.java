package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
  Page<Post> findByCourseIdAndApprovedOrderByCreatedAtDesc(String courseId, boolean approved, Pageable pageable);

  List<Post> findByCourseId(String courseId);
}