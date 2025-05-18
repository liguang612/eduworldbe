package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Choice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ChoiceRepository extends JpaRepository<Choice, String> {
  List<Choice> findByQuestionId(String questionId);

  Optional<Choice> findByQuestionIdAndValue(String questionId, String value);

  void deleteByQuestionId(String questionId);
}