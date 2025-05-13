package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, String> {
  List<Lecture> findBySubjectId(String subjectId);
}
