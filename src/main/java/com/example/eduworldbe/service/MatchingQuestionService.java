package com.example.eduworldbe.service;

import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.dto.request.MatchingColumnBatchRequest;
import com.example.eduworldbe.dto.request.MatchingPairBatchRequest;
import com.example.eduworldbe.dto.request.MatchingQuestionRequest;
import com.example.eduworldbe.dto.request.UpdateQuestionRequest;

import lombok.Data;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingQuestionService {
  @Autowired
  private MatchingColumnService matchingColumnService;

  @Autowired
  private MatchingPairService matchingPairService;

  @Autowired
  private QuestionService questionService;

  @Transactional
  public MatchingQuestionResult createMatchingQuestion(MatchingQuestionRequest request) {
    if (request.getSharedMediaId() != null) {
      UpdateQuestionRequest updateRequest = new UpdateQuestionRequest();
      updateRequest.setSharedMediaId(request.getSharedMediaId());
      questionService.update(request.getQuestionId(), updateRequest);
    }

    List<MatchingColumn> existingColumns = matchingColumnService.getByQuestionId(request.getQuestionId());
    List<MatchingPair> existingPairs = matchingPairService.getByQuestionId(request.getQuestionId());
    existingPairs.forEach(pair -> matchingPairService.delete(pair.getId()));
    existingColumns.forEach(column -> matchingColumnService.delete(column.getId()));

    // Create columns
    MatchingColumnBatchRequest columnRequest = new MatchingColumnBatchRequest();
    columnRequest.setQuestionId(request.getQuestionId());

    columnRequest.setLeft(request.getLeft().stream()
        .map(item -> {
          MatchingColumnBatchRequest.MatchingColumnItem newItem = new MatchingColumnBatchRequest.MatchingColumnItem();
          newItem.setLabel(item.getLabel());
          newItem.setOrderIndex(item.getOrderIndex());
          return newItem;
        })
        .collect(Collectors.toList()));

    columnRequest.setRight(request.getRight().stream()
        .map(item -> {
          MatchingColumnBatchRequest.MatchingColumnItem newItem = new MatchingColumnBatchRequest.MatchingColumnItem();
          newItem.setLabel(item.getLabel());
          newItem.setOrderIndex(item.getOrderIndex());
          return newItem;
        })
        .collect(Collectors.toList()));

    List<MatchingColumn> columns = matchingColumnService.createBatch(columnRequest);

    // Create pairs
    MatchingPairBatchRequest pairRequest = new MatchingPairBatchRequest();
    pairRequest.setQuestionId(request.getQuestionId());
    pairRequest.setPairs(request.getPairs().stream()
        .map(pair -> {
          MatchingPairBatchRequest.MatchingPairItem item = new MatchingPairBatchRequest.MatchingPairItem();

          // Only set from/to if indices are valid
          if (pair.getLeftIndex() >= 0 && pair.getRightIndex() >= 0
              && pair.getLeftIndex() < request.getLeft().size()
              && pair.getRightIndex() < request.getRight().size()) {
            item.setFrom(columns.get(pair.getLeftIndex()).getId());
            item.setTo(columns.get(pair.getRightIndex() + request.getLeft().size()).getId());
          } else {
            item.setFrom(null);
            item.setTo(null);
          }

          return item;
        })
        .collect(Collectors.toList()));

    List<MatchingPair> savedPairs = matchingPairService.createBatch(pairRequest);

    return new MatchingQuestionResult(columns, savedPairs);
  }

  @Data
  @AllArgsConstructor
  public static class MatchingQuestionResult {
    private List<MatchingColumn> columns;
    private List<MatchingPair> pairs;
  }
}