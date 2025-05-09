package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Subject;
import com.example.eduworldbe.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectService {
  @Autowired
  private SubjectRepository subjectRepository;

  public Subject create(Subject subject) {
    return subjectRepository.save(subject);
  }

  public List<Subject> getAll() {
    return subjectRepository.findAll();
  }

  public List<Subject> getByGrade(Integer grade) {
    return subjectRepository.findByGrade(grade);
  }
}
