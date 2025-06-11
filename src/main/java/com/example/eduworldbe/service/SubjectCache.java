package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Subject;
import com.example.eduworldbe.repository.SubjectRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SubjectCache {

  @Autowired
  private SubjectRepository subjectRepository;

  private static Map<String, Subject> subjectMap = new ConcurrentHashMap<>();

  @PostConstruct
  public void init() {
    List<Subject> subjects = subjectRepository.findAll();
    subjectMap = subjects.stream()
        .collect(Collectors.toMap(Subject::getId, Function.identity(), (oldValue, newValue) -> newValue,
            ConcurrentHashMap::new));
    System.out.println("Subject cache loaded with " + subjectMap.size() + " entries.");
  }

  public Subject getById(String id) {
    return subjectMap.get(id);
  }

  public Collection<Subject> getAll() {
    return subjectMap.values();
  }
}
