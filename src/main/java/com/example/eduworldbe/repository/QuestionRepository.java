package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, String> {
  @EntityGraph(attributePaths = { "sharedMedia" })
  Optional<Question> findWithSharedMediaById(String id);

  List<Question> findByType(String type);

  List<Question> findByCreatedBy(String createdBy);

  List<Question> findBySubjectId(String subjectId);

  List<Question> findByCreatedByAndSubjectId(String createdBy, String subjectId);

  List<Question> findBySharedMediaId(String sharedMediaId);
}