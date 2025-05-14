package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Subject;
import com.example.eduworldbe.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {
  @Autowired
  private SubjectService subjectService;

  @PostMapping
  public Subject create(@RequestBody Subject subject) {
    return subjectService.create(subject);
  }

  @PostMapping("/batch")
  public List<Subject> createBatch(@RequestBody List<Subject> subjects) {
    return subjects.stream()
        .map(subjectService::create)
        .toList();
  }

  @GetMapping
  public List<Subject> getAll() {
    return subjectService.getAll();
  }

  @GetMapping("/grade/{grade}")
  public List<Subject> getByGrade(@PathVariable Integer grade) {
    return subjectService.getByGrade(grade);
  }

  @GetMapping("/{id}")
  public Subject getById(@PathVariable String id) {
    return subjectService.getById(id);
  }
}
