package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.repository.ChoiceRepository;
import com.example.eduworldbe.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@Service
public class ChoiceService {
  @Autowired
  private ChoiceRepository choiceRepository;

  @Autowired
  private QuestionRepository questionRepository;

  public Choice create(Choice choice) {
    // Check if the question exists
    Optional<Question> question = questionRepository.findById(choice.getQuestionId());
    if (question.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          "Question with ID " + choice.getQuestionId() + " not found.");
    }

    // Check for duplicate value for the same questionId
    Optional<Choice> existingChoice = choiceRepository.findByQuestionIdAndValue(choice.getQuestionId(),
        choice.getValue());
    if (existingChoice.isPresent()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
          "Choice with the same value already exists for this question.");
    }
    return choiceRepository.save(choice);
  }

  public Optional<Choice> getById(String id) {
    return choiceRepository.findById(id);
  }

  public List<Choice> getAll() {
    return choiceRepository.findAll();
  }

  public List<Choice> getByQuestionId(String questionId) {
    return choiceRepository.findByQuestionId(questionId);
  }

  public Choice update(String id, Choice updated) {
    // Check if the question exists for the updated choice
    if (updated.getQuestionId() != null) {
      Optional<Question> question = questionRepository.findById(updated.getQuestionId());
      if (question.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
            "Question with ID " + updated.getQuestionId() + " not found.");
      }
    }

    Choice existing = choiceRepository.findById(id).orElseThrow();
    if (updated.getText() != null)
      existing.setText(updated.getText());
    if (updated.getValue() != null) {
      // Check for duplicate value for the same questionId during update, excluding
      // the current choice
      Optional<Choice> existingChoice = choiceRepository.findByQuestionIdAndValue(
          updated.getQuestionId() != null ? updated.getQuestionId() : existing.getQuestionId(),
          updated.getValue());
      if (existingChoice.isPresent() && !existingChoice.get().getId().equals(id)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Choice with the same value already exists for this question.");
      }
      existing.setValue(updated.getValue());
    }
    if (updated.getQuestionId() != null)
      existing.setQuestionId(updated.getQuestionId());
    if (updated.getOrderIndex() != null)
      existing.setOrderIndex(updated.getOrderIndex());
    return choiceRepository.save(existing);
  }

  public void delete(String id) {
    choiceRepository.deleteById(id);
  }
}