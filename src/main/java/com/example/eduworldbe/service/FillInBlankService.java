package com.example.eduworldbe.service;

import com.example.eduworldbe.model.FillInBlank;
import com.example.eduworldbe.repository.FillInBlankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FillInBlankService {
  @Autowired
  private FillInBlankRepository fillInBlankRepository;

  public FillInBlank create(FillInBlank blank) {
    return fillInBlankRepository.save(blank);
  }

  public Optional<FillInBlank> getById(String id) {
    return fillInBlankRepository.findById(id);
  }

  public List<FillInBlank> getByQuestionId(String questionId) {
    return fillInBlankRepository.findByQuestionId(questionId);
  }

  public FillInBlank update(String id, FillInBlank updated) {
    FillInBlank existing = fillInBlankRepository.findById(id).orElseThrow();
    if (updated.getBlankKey() != null)
      existing.setBlankKey(updated.getBlankKey());
    if (updated.getCorrectAnswer() != null)
      existing.setCorrectAnswer(updated.getCorrectAnswer());
    return fillInBlankRepository.save(existing);
  }

  public void delete(String id) {
    fillInBlankRepository.deleteById(id);
  }
}