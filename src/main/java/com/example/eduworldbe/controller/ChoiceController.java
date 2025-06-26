package com.example.eduworldbe.controller;

import com.example.eduworldbe.dto.request.ChoiceBatchRequest;
import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.service.ChoiceService;
import com.example.eduworldbe.util.AuthUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/choices")
public class ChoiceController {
  @Autowired
  private ChoiceService choiceService;

  @Autowired
  private AuthUtil authUtil;

  @PostMapping
  public ResponseEntity<Choice> create(@RequestBody Choice choice, HttpServletRequest request) {
    return ResponseEntity.ok(choiceService.create(choice));
  }

  @PostMapping("/batch")
  public ResponseEntity<List<Choice>> createBatch(@RequestBody ChoiceBatchRequest request,
      HttpServletRequest httpRequest) {
    return ResponseEntity.ok(choiceService.createBatch(request));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Choice> getById(@PathVariable String id) {
    return choiceService.getById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<Choice>> getAll() {
    return ResponseEntity.ok(choiceService.getAll());
  }

  @GetMapping("/question/{questionId}")
  public ResponseEntity<List<Choice>> getByQuestionId(@PathVariable String questionId) {
    return ResponseEntity.ok(choiceService.getByQuestionId(questionId));
  }

  @PutMapping("/{id}")
  public ResponseEntity<Choice> update(@PathVariable String id, @RequestBody Choice updated,
      HttpServletRequest request) {
    authUtil.requireActiveUser(request);
    return ResponseEntity.ok(choiceService.update(id, updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id, HttpServletRequest request) {
    authUtil.requireActiveUser(request);
    choiceService.delete(id);
    return ResponseEntity.ok().build();
  }
}