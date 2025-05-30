package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
  Page<Comment> findByPostIdOrderByCreatedAtDesc(String postId, Pageable pageable);
}