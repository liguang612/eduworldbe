package com.example.eduworldbe.service;

import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.repository.MatchingColumnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MatchingColumnService {
  @Autowired
  private MatchingColumnRepository matchingColumnRepository;

  public MatchingColumn create(MatchingColumn column) {
    return matchingColumnRepository.save(column);
  }

  public Optional<MatchingColumn> getById(String id) {
    return matchingColumnRepository.findById(id);
  }

  public List<MatchingColumn> getByQuestionId(String questionId) {
    return matchingColumnRepository.findByQuestionId(questionId);
  }

  public List<MatchingColumn> getByQuestionIdAndSide(String questionId, String side) {
    return matchingColumnRepository.findByQuestionIdAndSide(questionId, side);
  }

  public MatchingColumn update(String id, MatchingColumn updated) {
    MatchingColumn existing = matchingColumnRepository.findById(id).orElseThrow();
    if (updated.getContent() != null)
      existing.setContent(updated.getContent());
    if (updated.getSide() != null)
      existing.setSide(updated.getSide());
    if (updated.getOrderIndex() != null)
      existing.setOrderIndex(updated.getOrderIndex());
    return matchingColumnRepository.save(existing);
  }

  public void delete(String id) {
    matchingColumnRepository.deleteById(id);
  }
}