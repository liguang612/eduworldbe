package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.repository.ChoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChoiceService {
  @Autowired
  private ChoiceRepository choiceRepository;

  public Choice create(Choice choice) {
    return choiceRepository.save(choice);
  }

  public Optional<Choice> getById(String id) {
    return choiceRepository.findById(id);
  }

  public List<Choice> getByQuestionId(String questionId) {
    return choiceRepository.findByQuestionId(questionId);
  }

  public Choice update(String id, Choice updated) {
    Choice existing = choiceRepository.findById(id).orElseThrow();
    if (updated.getText() != null)
      existing.setText(updated.getText());
    if (updated.getImageUrl() != null)
      existing.setImageUrl(updated.getImageUrl());
    if (updated.getIsAnswer() != null)
      existing.setIsAnswer(updated.getIsAnswer());
    if (updated.getOrderIndex() != null)
      existing.setOrderIndex(updated.getOrderIndex());
    return choiceRepository.save(existing);
  }

  public void delete(String id) {
    choiceRepository.deleteById(id);
  }
}