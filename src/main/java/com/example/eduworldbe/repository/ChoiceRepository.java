package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, String> {
  List<Choice> findByQuestionId(String questionId);
}