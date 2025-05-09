package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, String> {
  List<Subject> findByGrade(Integer grade);
}
