package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Lecture;
import com.example.eduworldbe.repository.LectureRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.dto.LectureResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LectureService {
  @Autowired
  private LectureRepository lectureRepository;

  @Autowired
  private CourseService courseService;

  @Autowired
  private UserRepository userRepository;

  public Lecture create(Lecture lecture) {
    // Không xử lý courseId nữa
    return lectureRepository.save(lecture);
  }

  public Optional<Lecture> getById(String id) {
    return lectureRepository.findById(id);
  }

  public List<Lecture> getByCourseId(String courseId) {
    return lectureRepository.findByCourseId(courseId);
  }

  public Lecture update(String id, Lecture lecture) {
    lecture.setId(id);
    return lectureRepository.save(lecture);
  }

  public void delete(String id) {
    Lecture lecture = lectureRepository.findById(id).orElse(null);
    if (lecture != null) {
      // Xóa ID của bài giảng khỏi danh sách lectureIds của khóa học
      courseService.removeLectureFromCourseLectureIds(lecture.getCourseId(), id);
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

  // Thêm phương thức mới để add lecture vào course
  public void addLectureToCourse(String lectureId, String courseId) {
    // Kiểm tra lecture và course có tồn tại không
    lectureRepository.findById(lectureId)
        .orElseThrow(() -> new RuntimeException("Lecture not found"));

    // Thêm lecture vào course
    courseService.addLectureToCourseLectureIds(courseId, lectureId);
  }

  // Thêm phương thức xóa lecture khỏi course
  public void removeLectureFromCourse(String lectureId, String courseId) {
    courseService.removeLectureFromCourseLectureIds(courseId, lectureId);
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
    dto.setPdfUrl(lecture.getPdfUrl());
    dto.setCourseId(lecture.getCourseId());
    dto.setTeacher(
        lecture.getTeacherId() != null ? userRepository.findById(lecture.getTeacherId()).orElse(null) : null);
    return dto;
  }
}
