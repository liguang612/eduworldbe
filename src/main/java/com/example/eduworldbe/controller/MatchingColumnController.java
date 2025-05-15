package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.service.MatchingColumnService;
import com.example.eduworldbe.dto.MatchingColumnBatchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matching-columns")
public class MatchingColumnController {
  @Autowired
  private MatchingColumnService matchingColumnService;

  @PostMapping
  public ResponseEntity<MatchingColumn> create(@RequestBody MatchingColumn matchingColumn) {
    return ResponseEntity.ok(matchingColumnService.create(matchingColumn));
  }

  @GetMapping("/{id}")
  public ResponseEntity<MatchingColumn> getById(@PathVariable String id) {
    return matchingColumnService.getById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<MatchingColumn>> getAll() {
    return ResponseEntity.ok(matchingColumnService.getAll());
  }

  @GetMapping("/question/{questionId}")
  public ResponseEntity<List<MatchingColumn>> getByQuestionId(@PathVariable String questionId) {
    return ResponseEntity.ok(matchingColumnService.getByQuestionId(questionId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<MatchingColumn> update(@PathVariable String id, @RequestBody MatchingColumn updated) {
    return ResponseEntity.ok(matchingColumnService.update(id, updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    matchingColumnService.delete(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/batch")
  public ResponseEntity<List<MatchingColumn>> createBatch(@RequestBody MatchingColumnBatchRequest request) {
    List<MatchingColumn> columns = matchingColumnService.createBatch(request);
    return ResponseEntity.ok(columns);
  }
}