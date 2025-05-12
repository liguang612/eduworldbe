package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, String> {
  List<Chapter> findByCourseId(String courseId);
}