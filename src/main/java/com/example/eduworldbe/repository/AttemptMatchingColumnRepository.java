package com.example.eduworldbe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eduworldbe.model.AttemptMatchingColumn;

@Repository
public interface AttemptMatchingColumnRepository extends JpaRepository<AttemptMatchingColumn, String> {
  List<AttemptMatchingColumn> findByQuestionId(String questionId);

  void deleteByQuestionId(String questionId);
}