package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
  @Autowired
  private QuestionRepository questionRepository;

  public Question create(Question question) {
    question.setCreatedAt(new Date());
    question.setUpdatedAt(new Date());
    return questionRepository.save(question);
  }

  public Optional<Question> getById(String id) {
    return questionRepository.findById(id);
  }

  public List<Question> getAll() {
    return questionRepository.findAll();
  }

  public List<Question> getByType(Integer type) {
    return questionRepository.findByType(type);
  }

  public List<Question> getByCreatedBy(String createdBy) {
    return questionRepository.findByCreatedBy(createdBy);
  }

  public Question update(String id, Question updated) {
    Question existing = questionRepository.findById(id).orElseThrow();
    if (updated.getText() != null)
      existing.setText(updated.getText());
    if (updated.getType() != null)
      existing.setType(updated.getType());
    if (updated.getMediaUrl() != null)
      existing.setMediaUrl(updated.getMediaUrl());
    if (updated.getMediaType() != null)
      existing.setMediaType(updated.getMediaType());
    if (updated.getLevel() != null)
      existing.setLevel(updated.getLevel());
    if (updated.getCategories() != null)
      existing.setCategories(updated.getCategories());
    if (updated.getSolutionIds() != null)
      existing.setSolutionIds(updated.getSolutionIds());
    if (updated.getReviewIds() != null)
      existing.setReviewIds(updated.getReviewIds());
    existing.setUpdatedAt(new Date());
    return questionRepository.save(existing);
  }

  public void delete(String id) {
    questionRepository.deleteById(id);
  }
}