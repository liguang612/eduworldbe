package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Chapter;
import com.example.eduworldbe.repository.ChapterRepository;
import com.example.eduworldbe.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class ChapterService {
  @Autowired
  private ChapterRepository chapterRepository;

  @Autowired
  private CourseRepository courseRepository;

  public Chapter create(Chapter chapter) {
    Chapter createdChapter = chapterRepository.save(chapter);

    // Add chapterId to the corresponding Course's chapterIds list
    courseRepository.findById(chapter.getCourseId()).ifPresent(course -> {
      if (course.getChapterIds() == null) {
        course.setChapterIds(new ArrayList<>());
      }
      course.getChapterIds().add(createdChapter.getId());
      courseRepository.save(course);
    });

    return createdChapter;
  }

  public List<Chapter> getByCourseId(String courseId) {
    return chapterRepository.findByCourseId(courseId);
  }

  public Optional<Chapter> getById(String id) {
    return chapterRepository.findById(id);
  }

  public Chapter update(String id, Chapter updated) {
    updated.setId(id);
    return chapterRepository.save(updated);
  }

  public void delete(String id) {
    Optional<Chapter> chapterOptional = chapterRepository.findById(id);
    if (chapterOptional.isPresent()) {
      Chapter chapterToDelete = chapterOptional.get();
      String courseId = chapterToDelete.getCourseId();

      chapterRepository.deleteById(id);

      // Remove the chapterId from the corresponding Course's chapterIds list
      courseRepository.findById(courseId).ifPresent(course -> {
        if (course.getChapterIds() != null) {
          course.getChapterIds().remove(id);
          courseRepository.save(course);
        }
      });
    }
  }

  public void addLectureToChapter(String chapterId, String lectureId) {
    Chapter chapter = getById(chapterId).orElse(null);
    if (chapter != null) {
      if (chapter.getLectureIds() == null) {
        chapter.setLectureIds(new ArrayList<>());
      }
      chapter.getLectureIds().add(lectureId);
      update(chapterId, chapter);
    }
  }

  public void removeLectureFromChapter(String chapterId, String lectureId) {
    Chapter chapter = getById(chapterId).orElse(null);
    if (chapter != null && chapter.getLectureIds() != null) {
      chapter.getLectureIds().remove(lectureId);
      update(chapterId, chapter);
    }
  }
}