package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.FillInBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FillInBlankRepository extends JpaRepository<FillInBlank, String> {
  List<FillInBlank> findByQuestionId(String questionId);
}