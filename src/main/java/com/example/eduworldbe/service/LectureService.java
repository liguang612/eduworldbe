package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.response.LectureResponse;
import com.example.eduworldbe.model.Lecture;
import com.example.eduworldbe.model.Subject;
import com.example.eduworldbe.repository.LectureRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
public class LectureService {
  @Autowired
  private LectureRepository lectureRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SubjectService subjectService;

  @Autowired
  private ReviewService reviewService;

  public Lecture create(Lecture lecture) {
    return lectureRepository.save(lecture);
  }

  public Optional<Lecture> getById(String id) {
    return lectureRepository.findById(id);
  }

  public List<Lecture> getBySubjectId(String subjectId) {
    return lectureRepository.findBySubjectId(subjectId);
  }

  public Lecture update(String id, Lecture updated) {
    Lecture existingLecture = getById(id).orElseThrow(() -> new RuntimeException("Lecture not found"));

    if (updated.getName() != null) {
      existingLecture.setName(updated.getName());
    }
    if (updated.getDescription() != null) {
      existingLecture.setDescription(updated.getDescription());
    }
    if (updated.getContents() != null) {
      existingLecture.setContents(updated.getContents());
    }
    if (updated.getEndQuestions() != null) {
      existingLecture.setEndQuestions(updated.getEndQuestions());
    }
    if (updated.getCategories() != null) {
      existingLecture.setCategories(updated.getCategories());
    }
    if (updated.getSubjectId() != null) {
      existingLecture.setSubjectId(updated.getSubjectId());
    }
    if (updated.getDuration() != null) {
      existingLecture.setDuration(updated.getDuration());
    }

    return lectureRepository.save(existingLecture);
  }

  public void delete(String id) {
    Lecture lecture = lectureRepository.findById(id).orElse(null);
    if (lecture != null) {
      lectureRepository.deleteById(id);
    }
  }

  public Lecture addEndQuestion(String lectureId, String questionId) {
    Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
    if (!lecture.getEndQuestions().contains(questionId)) {
      lecture.getEndQuestions().add(questionId);
      return lectureRepository.save(lecture);
    }
    return lecture;
  }

  public Lecture removeEndQuestion(String lectureId, String questionId) {
    Lecture lecture = lectureRepository.findById(lectureId).orElseThrow();
    lecture.getEndQuestions().remove(questionId);
    return lectureRepository.save(lecture);
  }

  public List<Lecture> getAll() {
    return lectureRepository.findAll();
  }

  public LectureResponse toLectureResponse(Lecture lecture) {
    LectureResponse dto = new LectureResponse();
    dto.setId(lecture.getId());
    dto.setName(lecture.getName());
    dto.setDescription(lecture.getDescription());
    dto.setContents(lecture.getContents());
    dto.setEndQuestions(lecture.getEndQuestions());
    dto.setCategories(lecture.getCategories());
    dto.setSubjectId(lecture.getSubjectId());
    if (lecture.getSubjectId() != null) {
      Subject subject = subjectService.getById(lecture.getSubjectId());
      dto.setSubjectName(subject.getName());
      dto.setGrade(subject.getGrade());
    }
    dto.setTeacher(
        lecture.getTeacherId() != null ? userRepository.findById(lecture.getTeacherId()).orElse(null) : null);
    dto.setDuration(lecture.getDuration());
    dto.setAverageRating(reviewService.getAverageScore(2, lecture.getId()));
    return dto;
  }

  public List<Lecture> searchLecturesByName(List<Lecture> lectures, String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return lectures;
    }

    String[] searchTerms = keyword.toLowerCase().split("\\s+");

    return lectures.stream()
        .map(lecture -> {
          String lectureName = lecture.getName().toLowerCase();
          String lectureDescription = lecture.getDescription() != null ? lecture.getDescription().toLowerCase() : "";
          List<String> categories = lecture.getCategories() != null
              ? lecture.getCategories().stream()
                  .map(String::toLowerCase)
                  .toList()
              : List.of();

          double score = 0.0;

          // Tính điểm cho mỗi từ khóa
          for (String term : searchTerms) {
            double termScore = 0.0;

            // Khớp chính xác với tên
            if (lectureName.equals(term)) {
              termScore += 100.0;
            }
            // Khớp một phần với tên
            else if (lectureName.contains(term)) {
              double lengthRatio = (double) term.length() / lectureName.length();
              termScore += 80.0 * lengthRatio;
            }

            // Khớp với mô tả
            if (lectureDescription.contains(term)) {
              double lengthRatio = (double) term.length() / lectureDescription.length();
              termScore += 60.0 * lengthRatio;
            }

            // Khớp với categories
            for (String category : categories) {
              if (category.equals(term)) {
                termScore += 40.0;
              } else if (category.contains(term)) {
                double lengthRatio = (double) term.length() / category.length();
                termScore += 20.0 * lengthRatio;
              }
            }

            // Levenshtein distance cho tên
            int distance = StringUtil.calculateLevenshteinDistance(lectureName, term);
            if (distance <= 3) {
              termScore += Math.max(0, 30.0 * (1 - distance / 3.0));
            }

            // Kiểm tra Levenshtein distance cho từng từ trong tên
            String[] lectureWords = lectureName.split("\\s+");
            for (String word : lectureWords) {
              distance = StringUtil.calculateLevenshteinDistance(word, term);
              if (distance <= 2) {
                termScore += Math.max(0, 20.0 * (1 - distance / 2.0));
              }
            }

            score += termScore;
          }

          score = score / searchTerms.length;

          return new AbstractMap.SimpleEntry<>(lecture, score);
        })
        .filter(entry -> entry.getValue() > 0)
        .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // Sắp xếp theo điểm giảm dần
        .map(AbstractMap.SimpleEntry::getKey)
        .toList();
  }

  public List<Lecture> getByIdsInOrder(List<String> ids) {
    Map<String, Lecture> lectureMap = lectureRepository.findAllById(ids)
        .stream()
        .collect(Collectors.toMap(Lecture::getId, lecture -> lecture));

    return ids.stream()
        .map(lectureMap::get)
        .filter(Objects::nonNull)
        .toList();
  }

  public List<LectureResponse> searchLectures(
      String userId,
      Integer userRole,
      String subjectId,
      String grade,
      String sortBy,
      String sortOrder,
      String keyword) {

    List<Lecture> lectures;

    // Giáo viên
    if (userRole != null && userRole == 1) {
      if (subjectId != null && !subjectId.isEmpty()) {
        lectures = lectureRepository.findBySubjectId(subjectId).stream()
            .filter(lecture -> userId.equals(lecture.getTeacherId()))
            .collect(Collectors.toList());
      } else {
        lectures = lectureRepository.findAll().stream()
            .filter(lecture -> userId.equals(lecture.getTeacherId()))
            .collect(Collectors.toList());
      }
    }
    // Học sinh
    else {
      if (subjectId != null && !subjectId.isEmpty()) {
        lectures = lectureRepository.findLecturesFromEnrolledCoursesWithSubject(userId, subjectId);
      } else {
        lectures = lectureRepository.findLecturesFromEnrolledCourses(userId);

        if (grade != null && !grade.isEmpty()) {
          lectures = lectures.stream()
              .filter(lecture -> {
                try {
                  Subject subject = subjectService.getById(lecture.getSubjectId());
                  return subject != null && grade.equals(String.valueOf(subject.getGrade()));
                } catch (Exception e) {
                  return false;
                }
              })
              .collect(Collectors.toList());
        }
      }
    }

    if (keyword != null && !keyword.trim().isEmpty()) {
      lectures = searchLecturesByName(lectures, keyword);
    }

    // Convert to mutable list if it's immutable (from searchLecturesByName)
    lectures = new ArrayList<>(lectures);

    lectures.sort((l1, l2) -> {
      int comparison = 0;
      switch (sortBy.toLowerCase()) {
        case "rating":
          double rating1 = reviewService.getAverageScore(2, l1.getId());
          double rating2 = reviewService.getAverageScore(2, l2.getId());
          comparison = Double.compare(rating1, rating2);
          break;
        default:
          comparison = l1.getName().compareToIgnoreCase(l2.getName());
      }
      return sortOrder.equalsIgnoreCase("desc") ? -comparison : comparison;
    });

    return lectures.stream()
        .map(this::toLectureResponse)
        .collect(Collectors.toList());
  }
}
