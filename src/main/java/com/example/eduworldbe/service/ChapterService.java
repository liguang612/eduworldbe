package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Chapter;
import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.model.Notification;
import com.example.eduworldbe.model.NotificationType;
import com.example.eduworldbe.repository.ChapterRepository;
import com.example.eduworldbe.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Service
public class ChapterService {
  @Autowired
  private ChapterRepository chapterRepository;

  @Autowired
  private CourseRepository courseRepository;

  @Autowired
  private NotificationService notificationService;

  public Chapter create(Chapter chapter) {
    Chapter createdChapter = chapterRepository.save(chapter);

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

      Course course = courseRepository.findById(chapter.getCourseId()).orElse(null);
      if (course != null && course.getStudentIds() != null) {
        for (String studentId : course.getStudentIds()) {
          try {
            notificationService.createNotification(Notification.builder()
                .userId(studentId)
                .type(NotificationType.NEW_LECTURE_IN_COURSE)
                .actorId(course.getTeacherId())
                .lectureId(lectureId)
                .courseId(course.getId()));
          } catch (ExecutionException | InterruptedException e) {
            System.err.println(
                "Failed to create NEW_LECTURE_IN_COURSE notification for user " + studentId + ": " + e.getMessage());
            Thread.currentThread().interrupt();
          }
        }
      }
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