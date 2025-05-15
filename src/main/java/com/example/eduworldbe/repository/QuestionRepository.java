package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, String> {
  List<Question> findByType(String type);

  List<Question> findByCreatedBy(String createdBy);

  List<Question> findBySubjectId(String subjectId);

  List<Question> findByCreatedByAndSubjectId(String createdBy, String subjectId);
}