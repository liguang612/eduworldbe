package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.service.MatchingPairService;
import com.example.eduworldbe.dto.MatchingPairBatchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matching-pairs")
public class MatchingPairController {
  @Autowired
  private MatchingPairService matchingPairService;

  @PostMapping
  public ResponseEntity<MatchingPair> create(@RequestBody MatchingPair matchingPair) {
    return ResponseEntity.ok(matchingPairService.create(matchingPair));
  }

  @GetMapping("/{id}")
  public ResponseEntity<MatchingPair> getById(@PathVariable String id) {
    return matchingPairService.getById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<MatchingPair>> getAll() {
    return ResponseEntity.ok(matchingPairService.getAll());
  }

  @GetMapping("/question/{questionId}")
  public ResponseEntity<List<MatchingPair>> getByQuestionId(@PathVariable String questionId) {
    return ResponseEntity.ok(matchingPairService.getByQuestionId(questionId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<MatchingPair> update(@PathVariable String id, @RequestBody MatchingPair updated) {
    return ResponseEntity.ok(matchingPairService.update(id, updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    matchingPairService.delete(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/batch")
  public ResponseEntity<List<MatchingPair>> createBatch(@RequestBody MatchingPairBatchRequest request) {
    List<MatchingPair> pairs = matchingPairService.createBatch(request);
    return ResponseEntity.ok(pairs);
  }
}