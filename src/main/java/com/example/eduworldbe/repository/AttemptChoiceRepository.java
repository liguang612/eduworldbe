package com.example.eduworldbe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.eduworldbe.model.AttemptChoice;

@Repository
public interface AttemptChoiceRepository extends JpaRepository<AttemptChoice, String> {
  List<AttemptChoice> findByQuestionId(String questionId);

  void deleteByQuestionId(String questionId);
}