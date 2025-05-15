package com.example.eduworldbe.service;

import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.dto.MatchingQuestionRequest;
import com.example.eduworldbe.dto.MatchingColumnBatchRequest;
import com.example.eduworldbe.dto.MatchingPairBatchRequest;
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

  @Transactional
  public MatchingQuestionResult createMatchingQuestion(MatchingQuestionRequest request) {
    // Create columns
    MatchingColumnBatchRequest columnRequest = new MatchingColumnBatchRequest();
    columnRequest.setQuestionId(request.getQuestionId());

    // Convert left items
    columnRequest.setLeft(request.getLeft().stream()
        .map(item -> {
          MatchingColumnBatchRequest.MatchingColumnItem newItem = new MatchingColumnBatchRequest.MatchingColumnItem();
          newItem.setLabel(item.getLabel());
          newItem.setOrderIndex(item.getOrderIndex());
          return newItem;
        })
        .collect(Collectors.toList()));

    // Convert right items
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
          item.setFrom(columns.get(pair.getLeftIndex()).getId());
          item.setTo(columns.get(pair.getRightIndex() + request.getLeft().size()).getId());
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