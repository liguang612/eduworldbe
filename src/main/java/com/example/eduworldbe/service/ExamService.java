package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Exam;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.repository.ExamRepository;
import com.example.eduworldbe.repository.QuestionRepository;
import com.example.eduworldbe.dto.ExamResponse;
import com.example.eduworldbe.model.Attempt;
import com.example.eduworldbe.repository.AttemptRepository;
import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.model.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Random;

@Service
public class ExamService {
  @Autowired
  private ExamRepository examRepository;

  @Autowired
  private QuestionRepository questionRepository;

  @Autowired
  private ReviewService reviewService;

  @Autowired
  private AttemptRepository attemptRepository;

  @Autowired
  private CourseService courseService;

  @Autowired
  private SubjectService subjectService;

  public Exam create(Exam exam) {
    if (exam.getQuestionIds() == null) {
      exam.setQuestionIds(new ArrayList<>());
    }
    if (exam.getReviewIds() == null) {
      exam.setReviewIds(new ArrayList<>());
    }
    if (exam.getCategories() == null) {
      exam.setCategories(new ArrayList<>());
    }
    return examRepository.save(exam);
  }

  public Optional<Exam> getById(String id) {
    return examRepository.findById(id);
  }

  public List<Exam> getAll() {
    return examRepository.findAll();
  }

  public List<Exam> getByClassId(String classId) {
    return examRepository.findByClassId(classId);
  }

  public List<Exam> getByCreatedBy(String userId) {
    return examRepository.findByCreatedBy(userId);
  }

  public List<Exam> getActiveExamsByClassId(String classId) {
    return examRepository.findActiveExamsByClassId(classId, new Date());
  }

  public List<Exam> getPastExamsByClassId(String classId) {
    return examRepository.findPastExamsByClassId(classId, new Date());
  }

  public List<Exam> getUpcomingExamsByClassId(String classId) {
    return examRepository.findUpcomingExamsByClassId(classId, new Date());
  }

  @Transactional
  public Exam update(String id, Exam updatedExam) {
    Optional<Exam> existingExamOpt = examRepository.findById(id);
    if (existingExamOpt.isPresent()) {
      Exam existingExam = existingExamOpt.get();
      if (updatedExam.getTitle() != null) {
        existingExam.setTitle(updatedExam.getTitle());
      }
      if (updatedExam.getEasyCount() != null) {
        existingExam.setEasyCount(updatedExam.getEasyCount());
      }
      if (updatedExam.getEasyScore() != null) {
        existingExam.setEasyScore(updatedExam.getEasyScore());
      }
      if (updatedExam.getMediumCount() != null) {
        existingExam.setMediumCount(updatedExam.getMediumCount());
      }
      if (updatedExam.getMediumScore() != null) {
        existingExam.setMediumScore(updatedExam.getMediumScore());
      }
      if (updatedExam.getHardCount() != null) {
        existingExam.setHardCount(updatedExam.getHardCount());
      }
      if (updatedExam.getHardScore() != null) {
        existingExam.setHardScore(updatedExam.getHardScore());
      }
      if (updatedExam.getVeryHardCount() != null) {
        existingExam.setVeryHardCount(updatedExam.getVeryHardCount());
      }
      if (updatedExam.getVeryHardScore() != null) {
        existingExam.setVeryHardScore(updatedExam.getVeryHardScore());
      }

      // Thời gian mở đề và đóng đề có thể là null (vĩnh viễn)
      existingExam.setOpenTime(updatedExam.getOpenTime());
      existingExam.setCloseTime(updatedExam.getCloseTime());

      if (updatedExam.getMaxScore() != null) {
        existingExam.setMaxScore(updatedExam.getMaxScore());
      }
      if (updatedExam.getDurationMinutes() != null) {
        existingExam.setDurationMinutes(updatedExam.getDurationMinutes());
      }
      if (updatedExam.getShuffleQuestion() != null) {
        existingExam.setShuffleQuestion(updatedExam.getShuffleQuestion());
      }
      if (updatedExam.getShuffleChoice() != null) {
        existingExam.setShuffleChoice(updatedExam.getShuffleChoice());
      }
      if (updatedExam.getCategories() != null) {
        existingExam.setCategories(updatedExam.getCategories());
      }
      if (updatedExam.getAllowReview() != null) {
        existingExam.setAllowReview(updatedExam.getAllowReview());
      }
      if (updatedExam.getAllowViewAnswer() != null) {
        existingExam.setAllowViewAnswer(updatedExam.getAllowViewAnswer());
      }
      if (updatedExam.getMaxAttempts() != null) {
        existingExam.setMaxAttempts(updatedExam.getMaxAttempts());
      }
      if (updatedExam.getQuestionIds() != null) {
        existingExam.setQuestionIds(updatedExam.getQuestionIds());
      }
      return examRepository.save(existingExam);
    } else {
      throw new RuntimeException("Exam not found with id: " + id);
    }
  }

  @Transactional
  public void delete(String id) {
    examRepository.deleteById(id);
  }

  @Transactional
  public Exam addQuestionToExam(String examId, String questionId) {
    Optional<Exam> examOpt = examRepository.findById(examId);
    Optional<Question> questionOpt = questionRepository.findById(questionId);

    if (examOpt.isPresent() && questionOpt.isPresent()) {
      Exam exam = examOpt.get();
      if (exam.getQuestionIds() == null) {
        exam.setQuestionIds(new ArrayList<>());
      }

      if (!exam.getQuestionIds().contains(questionId)) {
        exam.getQuestionIds().add(questionId);
        return examRepository.save(exam);
      }
      return exam;
    } else {
      throw new RuntimeException("Exam or Question not found");
    }
  }

  @Transactional
  public Exam removeQuestionFromExam(String examId, String questionId) {
    Optional<Exam> examOpt = examRepository.findById(examId);

    if (examOpt.isPresent()) {
      Exam exam = examOpt.get();
      if (exam.getQuestionIds() != null) {
        exam.getQuestionIds().remove(questionId);
        return examRepository.save(exam);
      }
      return exam;
    } else {
      throw new RuntimeException("Exam not found");
    }
  }

  @Transactional
  public Exam addQuestionsToExam(String examId, List<String> questionIds) {
    Optional<Exam> examOpt = examRepository.findById(examId);

    if (examOpt.isPresent()) {
      Exam exam = examOpt.get();
      if (exam.getQuestionIds() == null) {
        exam.setQuestionIds(new ArrayList<>());
      }

      for (String questionId : questionIds) {
        if (!exam.getQuestionIds().contains(questionId)) {
          exam.getQuestionIds().add(questionId);
        }
      }

      return examRepository.save(exam);
    } else {
      throw new RuntimeException("Exam not found");
    }
  }

  public List<Question> getExamQuestions(String examId) {
    Optional<Exam> examOpt = examRepository.findById(examId);

    if (examOpt.isPresent()) {
      Exam exam = examOpt.get();
      if (exam.getQuestionIds() != null && !exam.getQuestionIds().isEmpty()) {
        return questionRepository.findAllById(exam.getQuestionIds());
      }
      return new ArrayList<>();
    } else {
      throw new RuntimeException("Exam not found");
    }
  }

  public List<Question> generateExamQuestions(String examId) {
    Optional<Exam> examOpt = examRepository.findById(examId);

    if (examOpt.isPresent()) {
      Exam exam = examOpt.get();
      if (exam.getQuestionIds() == null || exam.getQuestionIds().isEmpty()) {
        return new ArrayList<>();
      }

      List<Question> allQuestions = questionRepository.findAllById(exam.getQuestionIds());

      // Group questions by level
      List<Question> level1Questions = allQuestions.stream()
          .filter(q -> q.getLevel() != null && q.getLevel() == 1)
          .collect(Collectors.toList());

      List<Question> level2Questions = allQuestions.stream()
          .filter(q -> q.getLevel() != null && q.getLevel() == 2)
          .collect(Collectors.toList());

      List<Question> level3Questions = allQuestions.stream()
          .filter(q -> q.getLevel() != null && q.getLevel() == 3)
          .collect(Collectors.toList());

      List<Question> level4Questions = allQuestions.stream()
          .filter(q -> q.getLevel() != null && q.getLevel() == 4)
          .collect(Collectors.toList());

      List<Question> selectedQuestions = new ArrayList<>();
      Random random = new Random();

      // Select random questions for each level based on counts
      selectRandomQuestions(selectedQuestions, level1Questions, exam.getEasyCount(), random);
      selectRandomQuestions(selectedQuestions, level2Questions, exam.getMediumCount(), random);
      selectRandomQuestions(selectedQuestions, level3Questions, exam.getHardCount(), random);
      selectRandomQuestions(selectedQuestions, level4Questions, exam.getVeryHardCount(), random);

      // Shuffle questions if needed
      if (exam.getShuffleQuestion()) {
        java.util.Collections.shuffle(selectedQuestions);
      }

      return selectedQuestions;
    } else {
      throw new RuntimeException("Exam not found");
    }
  }

  private void selectRandomQuestions(List<Question> selectedQuestions, List<Question> questions,
      Integer count, Random random) {
    if (count == null || count <= 0 || questions.isEmpty()) {
      return;
    }

    // If we don't have enough questions of this level, use all available
    if (questions.size() <= count) {
      selectedQuestions.addAll(questions);
      return;
    }

    // Select random questions
    List<Question> availableQuestions = new ArrayList<>(questions);
    for (int i = 0; i < count && !availableQuestions.isEmpty(); i++) {
      int randomIndex = random.nextInt(availableQuestions.size());
      selectedQuestions.add(availableQuestions.remove(randomIndex));
    }
  }

  public ExamResponse toExamResponse(Exam exam, Course course) {
    ExamResponse response = new ExamResponse();
    response.setId(exam.getId());
    response.setClassId(exam.getClassId());
    response.setTitle(exam.getTitle());
    response.setOpenTime(exam.getOpenTime());
    response.setCloseTime(exam.getCloseTime());
    response.setMaxScore(exam.getMaxScore());
    response.setDurationMinutes(exam.getDurationMinutes());
    response.setShuffleQuestion(exam.getShuffleQuestion());
    response.setShuffleChoice(exam.getShuffleChoice());
    response.setCreatedBy(exam.getCreatedBy());
    response.setCreatedAt(exam.getCreatedAt());
    response.setUpdatedAt(exam.getUpdatedAt());
    response.setCategories(exam.getCategories());

    // Set level counts
    response.setEasyCount(exam.getEasyCount());
    response.setMediumCount(exam.getMediumCount());
    response.setHardCount(exam.getHardCount());
    response.setVeryHardCount(exam.getVeryHardCount());

    // Calculate total questions
    response.setTotalQuestions(
        (exam.getEasyCount() != null ? exam.getEasyCount() : 0) +
            (exam.getMediumCount() != null ? exam.getMediumCount() : 0) +
            (exam.getHardCount() != null ? exam.getHardCount() : 0) +
            (exam.getVeryHardCount() != null ? exam.getVeryHardCount() : 0));

    // Calculate question bank size
    response.setQuestionBankSize(exam.getQuestionIds() != null ? exam.getQuestionIds().size() : 0);

    // Get average rating
    response.setAverageRating(reviewService.getAverageScore(4, exam.getId()));
    response.setReviewCount(reviewService.getReviewCount(4, exam.getId()));

    response.setAllowReview(exam.getAllowReview());
    response.setAllowViewAnswer(exam.getAllowViewAnswer());
    response.setMaxAttempts(exam.getMaxAttempts());

    // Set scores for each level
    response.setEasyScore(exam.getEasyScore());
    response.setMediumScore(exam.getMediumScore());
    response.setHardScore(exam.getHardScore());
    response.setVeryHardScore(exam.getVeryHardScore());

    // Add Course information
    if (course != null) {
      response.setClassName(course.getName()); // Giả định Course có phương thức getName()
      // Lấy thông tin Subject từ subjectId của Course
      try {
        Subject subject = subjectService.getById(course.getSubjectId());
        response.setSubjectName(subject.getName());
        response.setGrade(subject.getGrade());
      } catch (RuntimeException e) {
        // Xử lý trường hợp không tìm thấy Subject
        System.err
            .println("Subject not found for course: " + course.getId() + ", subjectId: " + course.getSubjectId());
      }
    }

    return response;
  }

  public ExamResponse toExamResponse(Exam exam) {
    Course course = courseService.getById(exam.getClassId()).orElse(null);
    return toExamResponse(exam, course);
  }

  public Attempt createAttempt(String examId, String userId) {
    Optional<Exam> examOpt = examRepository.findById(examId);
    if (examOpt.isPresent()) {
      Exam exam = examOpt.get();
      Attempt attempt = new Attempt();
      attempt.setExamId(examId);
      attempt.setUserId(userId);
      attempt.setStartTime(new Date());
      attempt.setEndTime(new Date(System.currentTimeMillis() + exam.getDurationMinutes() * 60000));
      attempt.setSubmitted(false);
      attempt.setScore(0.0);
      attempt.setPercentageScore(0.0);

      // Copy điểm số từ Exam sang Attempt
      attempt.setEasyScore(exam.getEasyScore());
      attempt.setMediumScore(exam.getMediumScore());
      attempt.setHardScore(exam.getHardScore());
      attempt.setVeryHardScore(exam.getVeryHardScore());

      return attemptRepository.save(attempt);
    } else {
      throw new RuntimeException("Exam not found");
    }
  }

  public List<ExamResponse> getUpcomingExams(String userId, Integer userRole, Integer total) {
    Date currentTime = new Date();
    List<Exam> exams;

    if (userRole != null && userRole == 1) {
      // Nếu là giáo viên, lấy các exam do họ tạo và sắp/đang diễn ra
      exams = examRepository.findUpcomingExamsByTeacher(userId, currentTime);
    } else {
      // Nếu là học sinh, lấy tất cả exam sắp/đang diễn ra và lọc ở service
      exams = examRepository.findAllUpcomingExams(currentTime);

      // Lọc các exam thuộc khóa học mà học sinh đang tham gia
      if (userId != null) {
        List<Course> enrolledCourses = courseService.getEnrolledCourses(userId);
        List<String> enrolledCourseIds = enrolledCourses.stream()
            .map(Course::getId)
            .toList();

        exams = exams.stream()
            .filter(exam -> enrolledCourseIds.contains(exam.getClassId()))
            .toList();
      }
    }

    return exams.stream()
        .map(exam -> {
          Course course = courseService.getById(exam.getClassId()).orElse(null);
          return toExamResponse(exam, course);
        })
        .limit(total)
        .toList();
  }
}