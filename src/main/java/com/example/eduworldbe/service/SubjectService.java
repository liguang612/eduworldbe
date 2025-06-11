package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Subject;
import com.example.eduworldbe.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {
  @Autowired
  private SubjectRepository subjectRepository;

  @Autowired
  private SubjectCache subjectCache;

  public Subject create(Subject subject) {
    return subjectRepository.save(subject);
  }

  public List<Subject> getAll() {
    return List.copyOf(subjectCache.getAll());
  }

  public List<Subject> getByGrade(Integer grade) {
    return subjectCache.getAll().stream()
        .filter(subject -> grade.equals(subject.getGrade()))
        .collect(Collectors.toList());
  }

  public Subject getById(String id) {
    Subject subject = subjectCache.getById(id);
    if (subject == null) {
      throw new RuntimeException("Subject not found from cache with id: " + id);
    }
    return subject;
  }
}
