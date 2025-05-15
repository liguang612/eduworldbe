package com.example.eduworldbe.service;

import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.repository.MatchingColumnRepository;
import com.example.eduworldbe.dto.MatchingColumnBatchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MatchingColumnService {
  @Autowired
  private MatchingColumnRepository matchingColumnRepository;

  public MatchingColumn create(MatchingColumn matchingColumn) {
    return matchingColumnRepository.save(matchingColumn);
  }

  public Optional<MatchingColumn> getById(String id) {
    return matchingColumnRepository.findById(id);
  }

  public List<MatchingColumn> getAll() {
    return matchingColumnRepository.findAll();
  }

  public List<MatchingColumn> getByQuestionId(String questionId) {
    return matchingColumnRepository.findByQuestionId(questionId);
  }

  public List<MatchingColumn> getByQuestionIdAndSide(String questionId, String side) {
    return matchingColumnRepository.findByQuestionIdAndSide(questionId, side);
  }

  public MatchingColumn update(String id, MatchingColumn updated) {
    MatchingColumn existing = matchingColumnRepository.findById(id).orElseThrow();
    if (updated.getLabel() != null)
      existing.setLabel(updated.getLabel());
    if (updated.getQuestionId() != null)
      existing.setQuestionId(updated.getQuestionId());
    if (updated.getSide() != null)
      existing.setSide(updated.getSide());
    if (updated.getOrderIndex() != null)
      existing.setOrderIndex(updated.getOrderIndex());
    return matchingColumnRepository.save(existing);
  }

  public void delete(String id) {
    matchingColumnRepository.deleteById(id);
  }

  @Transactional
  public List<MatchingColumn> createBatch(MatchingColumnBatchRequest request) {
    List<MatchingColumn> leftColumns = request.getLeft().stream()
        .map(item -> {
          MatchingColumn column = new MatchingColumn();
          column.setQuestionId(request.getQuestionId());
          column.setSide("left");
          column.setLabel(item.getLabel());
          column.setOrderIndex(item.getOrderIndex());
          return column;
        })
        .collect(Collectors.toList());

    List<MatchingColumn> rightColumns = request.getRight().stream()
        .map(item -> {
          MatchingColumn column = new MatchingColumn();
          column.setQuestionId(request.getQuestionId());
          column.setSide("right");
          column.setLabel(item.getLabel());
          column.setOrderIndex(item.getOrderIndex());
          return column;
        })
        .collect(Collectors.toList());

    leftColumns.addAll(rightColumns);
    return matchingColumnRepository.saveAll(leftColumns);
  }
}