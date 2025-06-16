package com.example.eduworldbe.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.eduworldbe.dto.response.ExamAttemptDetailResponse;
import com.example.eduworldbe.dto.response.ExamAttemptListResponse;
import com.example.eduworldbe.dto.response.QuestionDetailResponse;
import com.example.eduworldbe.exception.ResourceNotFoundException;
import com.example.eduworldbe.model.AttemptChoice;
import com.example.eduworldbe.model.AttemptMatchingColumn;
import com.example.eduworldbe.model.AttemptMatchingPair;
import com.example.eduworldbe.model.AttemptQuestion;
import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.model.Exam;
import com.example.eduworldbe.model.ExamAttempt;
import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.AttemptChoiceRepository;
import com.example.eduworldbe.repository.AttemptMatchingColumnRepository;
import com.example.eduworldbe.repository.AttemptMatchingPairRepository;
import com.example.eduworldbe.repository.AttemptQuestionRepository;
import com.example.eduworldbe.repository.ChoiceRepository;
import com.example.eduworldbe.repository.ExamRepository;
import com.example.eduworldbe.repository.ExamAttemptRepository;
import com.example.eduworldbe.repository.MatchingColumnRepository;
import com.example.eduworldbe.repository.MatchingPairRepository;
import com.example.eduworldbe.repository.QuestionRepository;
import com.example.eduworldbe.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
@Transactional
@Slf4j
public class ExamAttemptService {
  private final ExamAttemptRepository examAttemptRepository;
  private final AttemptQuestionRepository attemptQuestionRepository;
  private final AttemptChoiceRepository attemptChoiceRepository;
  private final AttemptMatchingColumnRepository attemptMatchingColumnRepository;
  private final AttemptMatchingPairRepository attemptMatchingPairRepository;
  private final ExamRepository examRepository;
  private final QuestionRepository questionRepository;
  private final ChoiceRepository choiceRepository;
  private final MatchingColumnRepository matchingColumnRepository;
  private final MatchingPairRepository matchingPairRepository;
  private final UserRepository userRepository;

  @Autowired
  private CourseService courseService;
  @Autowired
  private QuestionService questionService;
  @Autowired
  private ObjectMapper objectMapper;

  public ExamAttemptService(
      ExamAttemptRepository examAttemptRepository,
      AttemptQuestionRepository attemptQuestionRepository,
      AttemptChoiceRepository attemptChoiceRepository,
      AttemptMatchingColumnRepository attemptMatchingColumnRepository,
      AttemptMatchingPairRepository attemptMatchingPairRepository,
      ExamRepository examRepository,
      QuestionRepository questionRepository,
      ChoiceRepository choiceRepository,
      MatchingColumnRepository matchingColumnRepository,
      MatchingPairRepository matchingPairRepository,
      UserRepository userRepository) {
    this.examAttemptRepository = examAttemptRepository;
    this.attemptQuestionRepository = attemptQuestionRepository;
    this.attemptChoiceRepository = attemptChoiceRepository;
    this.attemptMatchingColumnRepository = attemptMatchingColumnRepository;
    this.attemptMatchingPairRepository = attemptMatchingPairRepository;
    this.examRepository = examRepository;
    this.questionRepository = questionRepository;
    this.choiceRepository = choiceRepository;
    this.matchingColumnRepository = matchingColumnRepository;
    this.matchingPairRepository = matchingPairRepository;
    this.userRepository = userRepository;
  }

  public ExamAttempt startAttempt(String userId, String examId) {
    // Kiểm tra xem user đã có attempt đang làm dở chưa (in_progress)
    List<ExamAttempt> existingAttempts = examAttemptRepository.findByUserIdAndExamIdAndStatus(userId, examId,
        "in_progress");
    if (!existingAttempts.isEmpty()) {
      if (existingAttempts.size() > 1) {
        List<String> attemptIdsToDelete = existingAttempts.stream()
            .skip(1)
            .map(ExamAttempt::getId)
            .collect(Collectors.toList());

        log.warn("Tìm thấy {} bản ghi trùng lặp cho userId: {}, examId: {}. Đang xóa {} bản ghi thừa.",
            existingAttempts.size(), userId, examId, attemptIdsToDelete.size());

        deleteAttempts(attemptIdsToDelete);
      }

      ExamAttempt attempt = existingAttempts.get(0);

      // Kiểm tra thời gian để tự động nộp bài
      Date now = new Date();
      long timeElapsed = now.getTime() - attempt.getStartTime().getTime();
      long timeElapsedMinutes = timeElapsed / (60 * 1000);

      if (timeElapsedMinutes >= attempt.getDuration()) {
        attempt.setStatus("submitted");
        attempt.setEndTime(new Date(attempt.getStartTime().getTime() + (long) attempt.getDuration() * 60 * 1000));
        attempt.setScore(calculateScore(attempt.getId()));

        attempt = examAttemptRepository.save(attempt);

        return attempt;
      }
      return attempt;
    }

    // 1. Lấy thông tin đề thi
    Exam exam = examRepository.findById(examId)
        .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

    // Kiểm tra số lần thi
    long attemptCount = examAttemptRepository.countByUserIdAndExamId(userId, examId);
    if (attemptCount >= exam.getMaxAttempts()) {
      ExamAttempt outOfAttempt = new ExamAttempt();
      outOfAttempt.setExamId(examId);
      outOfAttempt.setClassId(exam.getClassId());
      outOfAttempt.setTitle(exam.getTitle());
      outOfAttempt.setStatus("out_of_attempt");
      return outOfAttempt;
    }

    // 2. Tạo attempt mới
    ExamAttempt attempt = new ExamAttempt();
    attempt.setExamId(examId);
    attempt.setClassId(exam.getClassId());
    attempt.setDuration(exam.getDurationMinutes());
    attempt.setMaxScore(exam.getMaxScore().doubleValue());
    attempt.setTitle(exam.getTitle());
    attempt.setEasyScore(exam.getEasyScore());
    attempt.setMediumScore(exam.getMediumScore());
    attempt.setHardScore(exam.getHardScore());
    attempt.setVeryHardScore(exam.getVeryHardScore());
    attempt.setUserId(userId);
    attempt.setStartTime(new Date());
    attempt.setShuffleChoice(exam.getShuffleChoice());
    attempt.setShuffleQuestion(exam.getShuffleQuestion());
    attempt.setStatus("in_progress");
    attempt = examAttemptRepository.save(attempt);

    // 3. Lấy tất cả câu hỏi và phân loại theo level
    List<Question> allQuestions = questionRepository.findAllById(exam.getQuestionIds());
    Map<Integer, List<Question>> questionsByLevel = allQuestions.stream()
        .collect(Collectors.groupingBy(Question::getLevel));

    // 4. Chọn ngẫu nhiên câu hỏi theo số lượng yêu cầu cho từng level
    List<Question> selectedQuestions = new ArrayList<>();

    // Chọn câu hỏi level 1 (Nhận biết)
    if (questionsByLevel.containsKey(1)) {
      List<Question> easyQuestions = questionsByLevel.get(1);
      Collections.shuffle(easyQuestions);

      selectedQuestions.addAll(easyQuestions.subList(0, Math.min(exam.getEasyCount(), easyQuestions.size())));
    }

    // Chọn câu hỏi level 2 (Thông hiểu)
    if (questionsByLevel.containsKey(2)) {
      List<Question> mediumQuestions = questionsByLevel.get(2);
      Collections.shuffle(mediumQuestions);

      selectedQuestions.addAll(mediumQuestions.subList(0, Math.min(exam.getMediumCount(), mediumQuestions.size())));
    }

    // Chọn câu hỏi level 3 (Vận dụng)
    if (questionsByLevel.containsKey(3)) {
      List<Question> hardQuestions = questionsByLevel.get(3);
      Collections.shuffle(hardQuestions);

      selectedQuestions.addAll(hardQuestions.subList(0, Math.min(exam.getHardCount(), hardQuestions.size())));
    }

    // Chọn câu hỏi level 4 (Vận dụng cao)
    if (questionsByLevel.containsKey(4)) {
      List<Question> veryHardQuestions = questionsByLevel.get(4);
      Collections.shuffle(veryHardQuestions);

      selectedQuestions
          .addAll(veryHardQuestions.subList(0, Math.min(exam.getVeryHardCount(), veryHardQuestions.size())));
    }

    // 5. Copy selected questions vào attempt
    for (Question question : selectedQuestions) {
      AttemptQuestion attemptQuestion = new AttemptQuestion();
      attemptQuestion.setAttemptId(attempt.getId());
      attemptQuestion.setQuestionId(question.getId());
      attemptQuestion.setLevel(question.getLevel());
      attemptQuestion.setType(question.getType());
      attemptQuestion.setTitle(question.getTitle());
      attemptQuestion.setSharedMedia(question.getSharedMedia());
      attemptQuestion.setAnswer("");
      attemptQuestion = attemptQuestionRepository.save(attemptQuestion);

      // 6. Copy choices hoặc matching data tùy theo loại câu hỏi
      if ("itemConnector".equals(question.getType())) {
        // Copy matching columns
        List<MatchingColumn> matchingColumns = matchingColumnRepository.findByQuestionId(question.getId());
        for (MatchingColumn column : matchingColumns) {
          AttemptMatchingColumn attemptColumn = new AttemptMatchingColumn();
          attemptColumn.setQuestionId(attemptQuestion.getId());
          attemptColumn.setMatchingColumnId(column.getId());
          attemptColumn.setSide(column.getSide());
          attemptColumn.setLabel(column.getLabel());
          attemptMatchingColumnRepository.save(attemptColumn);
        }

        // Copy matching pairs
        List<MatchingPair> matchingPairs = matchingPairRepository.findByQuestionId(question.getId());
        for (MatchingPair pair : matchingPairs) {
          AttemptMatchingPair attemptPair = new AttemptMatchingPair();
          attemptPair.setQuestionId(attemptQuestion.getId());
          attemptPair.setMatchingPairId(pair.getId());
          attemptPair.setSource(pair.getFrom());
          attemptPair.setTarget(pair.getTo());
          attemptMatchingPairRepository.save(attemptPair);
        }
      } else {
        // Copy choices
        List<Choice> choices = choiceRepository.findByQuestionId(question.getId());
        for (Choice choice : choices) {
          AttemptChoice attemptChoice = new AttemptChoice();
          attemptChoice.setQuestionId(attemptQuestion.getId());
          attemptChoice.setChoiceId(choice.getId());
          attemptChoice.setIsCorrect(choice.getIsCorrect());
          attemptChoice.setOrderIndex(choice.getOrderIndex());
          attemptChoice.setText(choice.getText());
          attemptChoice.setValue(choice.getValue());
          attemptChoiceRepository.save(attemptChoice);
        }
      }
    }

    // Attempt mới thì status là started (Phải copyWith vì nếu như setStatus ở
    // instance dùng để save thì DB biến đổi theo :/)
    ExamAttempt newAttempt = attempt.copyWith();
    newAttempt.setStatus("started");
    return newAttempt;
  }

  public void saveAnswer(String attemptId, String questionId, String answer) {
    AttemptQuestion attemptQuestion = attemptQuestionRepository
        .findByAttemptIdAndQuestionId(attemptId, questionId)
        .orElseThrow(() -> new ResourceNotFoundException("Question not found in attempt"));
    attemptQuestion.setAnswer(answer);
    attemptQuestionRepository.save(attemptQuestion);
  }

  public void submitAttempt(String attemptId) {
    ExamAttempt attempt = examAttemptRepository.findById(attemptId)
        .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

    // Tính điểm và cập nhật attempt
    Double score = calculateScore(attemptId);
    attempt.setStatus("submitted");
    attempt.setEndTime(new Date());
    attempt.setScore(score);
    examAttemptRepository.save(attempt);
  }

  private Double calculateScore(String attemptId) {
    // 1. Lấy thông tin attempt
    ExamAttempt attempt = examAttemptRepository.findById(attemptId)
        .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

    // 2. Lấy danh sách câu hỏi của attempt
    List<AttemptQuestion> attemptQuestions = attemptQuestionRepository.findByAttemptId(attemptId);
    double totalScore = 0.0;

    // 3. Chấm điểm từng câu
    for (AttemptQuestion attemptQuestion : attemptQuestions) {
      // Lấy thông tin câu hỏi gốc
      Question question = questionRepository.findById(attemptQuestion.getQuestionId())
          .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

      // Lấy câu trả lời của user
      String userAnswer = attemptQuestion.getAnswer();

      // Kiểm tra đáp án và tính điểm dựa trên level của câu hỏi
      boolean isCorrect = checkAnswer(question, userAnswer);
      if (isCorrect) {
        switch (question.getLevel()) {
          case 1:
            totalScore += attempt.getEasyScore();
            break;
          case 2:
            totalScore += attempt.getMediumScore();
            break;
          case 3:
            totalScore += attempt.getHardScore();
            break;
          case 4:
            totalScore += attempt.getVeryHardScore();
            break;
        }
      }
    }

    return totalScore;
  }

  private boolean checkAnswer(Question question, String userAnswer) {
    if (userAnswer == null || userAnswer.isEmpty()) {
      return false;
    }

    try {
      // Chuyển đổi Question thành QuestionDetailResponse
      QuestionDetailResponse questionDetail = new QuestionDetailResponse(question);

      // Parse userAnswer thành Object tương ứng với loại câu hỏi
      Object parsedAnswer;
      switch (question.getType()) {
        case "radio":
        case "shortAnswer":
          parsedAnswer = userAnswer;
          break;
        case "checkbox":
        case "ranking":
          parsedAnswer = objectMapper.readValue(userAnswer, new TypeReference<List<String>>() {
          });
          break;
        case "itemConnector":
          parsedAnswer = objectMapper.readValue(userAnswer, new TypeReference<List<Map<String, String>>>() {
          });
          break;
        default:
          return false;
      }

      // Sử dụng QuestionService để kiểm tra đáp án
      return questionService.checkAnswer(questionDetail, parsedAnswer);
    } catch (Exception e) {
      log.error("Error checking answer", e);
      return false;
    }
  }

  @Transactional
  public void deleteAttempt(String attemptId) {
    List<AttemptQuestion> attemptQuestions = attemptQuestionRepository.findByAttemptId(attemptId);
    for (AttemptQuestion attemptQuestion : attemptQuestions) {
      attemptChoiceRepository.deleteByQuestionId(attemptQuestion.getId());
      attemptMatchingColumnRepository.deleteByQuestionId(attemptQuestion.getId());
      attemptMatchingPairRepository.deleteByQuestionId(attemptQuestion.getId());

      attemptQuestionRepository.delete(attemptQuestion);
    }

    examAttemptRepository.deleteById(attemptId);
  }

  @Transactional
  public void deleteAttempts(List<String> attemptIds) {
    for (String attemptId : attemptIds) {
      deleteAttempt(attemptId);
    }
  }

  public Map<String, String> getSavedAnswers(String attemptId) {
    List<AttemptQuestion> attemptQuestions = attemptQuestionRepository.findByAttemptId(attemptId);
    return attemptQuestions.stream()
        .collect(Collectors.toMap(
            AttemptQuestion::getQuestionId,
            AttemptQuestion::getAnswer));
  }

  public List<ExamAttemptListResponse> getAttemptsByUserAndStatus(String userId, String status) {
    List<ExamAttempt> attempts;
    if (status != null && !status.isEmpty()) {
      attempts = examAttemptRepository.findByUserIdAndStatus(userId, status);
    } else {
      attempts = examAttemptRepository.findByUserId(userId);
    }

    return attempts.stream().filter(attempt -> {
      if (attempt.getStatus().equals("in_progress")) {
        // Kiểm tra thời gian để tự động nộp bài
        Date now = new Date();
        long timeElapsed = now.getTime() - attempt.getStartTime().getTime();
        long timeElapsedMinutes = timeElapsed / (60 * 1000);

        if (timeElapsedMinutes >= attempt.getDuration()) {
          attempt.setStatus("submitted");
          attempt.setEndTime(new Date(attempt.getStartTime().getTime() + (long) attempt.getDuration() * 60 * 1000));
          attempt.setScore(calculateScore(attempt.getId()));
          examAttemptRepository.save(attempt);
          return true;
        } else {
          // Chưa vượt quá thời gian, loại bỏ khỏi kết quả
          return false;
        }
      }

      return "submitted".equals(attempt.getStatus());
    }).map(attempt -> {
      ExamAttemptListResponse response = new ExamAttemptListResponse();
      response.setId(attempt.getId());
      response.setTitle(attempt.getTitle());
      response.setScore(attempt.getScore());
      response.setMaxScore(attempt.getMaxScore());
      response.setStartTime(attempt.getStartTime());
      response.setEndTime(attempt.getEndTime());
      response.setStatus(attempt.getStatus());
      response.setClassId(attempt.getClassId());
      response.setClassName(courseService.getById(attempt.getClassId()).get().getName());
      response.setExamId(attempt.getExamId());

      return response;
    }).collect(Collectors.toList());
  }

  public ExamAttemptDetailResponse getAttemptDetail(String attemptId, User user) {
    ExamAttempt attempt = examAttemptRepository.findById(attemptId)
        .orElseThrow(() -> new ResourceNotFoundException("Attempt not found"));

    // Kiểm tra thời gian để tự động nộp bài
    if (attempt.getStatus().equals("in_progress")) {
      Date now = new Date();
      long timeElapsed = now.getTime() - attempt.getStartTime().getTime();
      long timeElapsedMinutes = timeElapsed / (60 * 1000);

      if (timeElapsedMinutes >= attempt.getDuration()) {
        attempt.setStatus("submitted");
        attempt.setEndTime(new Date(attempt.getStartTime().getTime() + (long) attempt.getDuration() * 60 * 1000));
        attempt.setScore(calculateScore(attempt.getId()));
        examAttemptRepository.save(attempt);
      }
    }

    boolean allowReview = true;
    boolean showRightAnswer = true;

    try {
      Exam exam = examRepository.findById(attempt.getExamId())
          .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

      if (user.getRole() == 0) {
        allowReview = (exam.getAllowReview() == true);
        showRightAnswer = (exam.getAllowViewAnswer() == true);
      }
    } catch (Exception e) {
      System.out.println("Exam not found!!!");
      e.printStackTrace();
    }

    ExamAttemptDetailResponse response = new ExamAttemptDetailResponse();
    response.setId(attempt.getId());
    response.setTitle(attempt.getTitle());
    response.setScore(attempt.getScore());
    response.setMaxScore(attempt.getMaxScore());
    response.setStartTime(attempt.getStartTime());
    response.setEndTime(attempt.getEndTime());
    response.setStatus(attempt.getStatus());
    response.setClassId(attempt.getClassId());
    response.setClassName(courseService.getById(attempt.getClassId()).get().getName());
    response.setDuration(attempt.getDuration());

    if (allowReview == false && response.getStatus().equals("submitted")) {
      ExamAttemptDetailResponse _response = new ExamAttemptDetailResponse();
      _response.setId("-1");
      return _response;
    }

    // Lấy thông tin học sinh
    User student = userRepository.findById(attempt.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    response.setUserId(student.getId());
    response.setStudentName(student.getName());
    response.setStudentEmail(student.getEmail());
    response.setStudentAvatar(student.getAvatar());
    response.setStudentSchool(student.getSchool());

    // Lấy danh sách câu hỏi và câu trả lời
    List<AttemptQuestion> attemptQuestions = attemptQuestionRepository.findByAttemptId(attemptId);
    Map<String, String> answers = attemptQuestions.stream()
        .collect(Collectors.toMap(
            AttemptQuestion::getQuestionId,
            AttemptQuestion::getAnswer));

    List<QuestionDetailResponse> questions = attemptQuestions.stream()
        .map(aq -> {
          Question question = questionRepository.findById(aq.getQuestionId())
              .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
          QuestionDetailResponse questionDetail = new QuestionDetailResponse(question);

          // Lấy danh sách lựa chọn từ attempt_choices
          if (!"itemConnector".equals(question.getType())) {
            List<AttemptChoice> attemptChoices = attemptChoiceRepository.findByQuestionId(aq.getId());
            questionDetail.setChoices(attemptChoices.stream()
                .map(ac -> {
                  Choice choice = new Choice();
                  choice.setId(ac.getChoiceId());
                  choice.setText(ac.getText());
                  choice.setValue(ac.getValue());
                  choice.setIsCorrect(attempt.getStatus().equals("submitted") ? ac.getIsCorrect() : null);
                  choice.setOrderIndex(attempt.getStatus().equals("submitted") ? ac.getOrderIndex() : null);
                  return choice;
                })
                .collect(Collectors.toList()));
          } else {
            List<AttemptMatchingColumn> attemptColumns = attemptMatchingColumnRepository.findByQuestionId(aq.getId());

            List<MatchingColumn> columns = attemptColumns.stream()
                .map(ac -> {
                  MatchingColumn column = new MatchingColumn();
                  column.setId(ac.getMatchingColumnId());
                  column.setSide(ac.getSide());
                  column.setLabel(ac.getLabel());
                  return column;
                })
                .collect(Collectors.toList());

            if (attempt.getStatus().equals("submitted")) {
              List<AttemptMatchingPair> attemptPairs = attemptMatchingPairRepository.findByQuestionId(aq.getId());
              List<MatchingPair> pairs = attempt.getStatus().equals("submitted") ? attemptPairs.stream()
                  .map(ap -> {
                    MatchingPair pair = new MatchingPair();
                    pair.setId(ap.getMatchingPairId());
                    pair.setFrom(ap.getSource());
                    pair.setTo(ap.getTarget());
                    return pair;
                  })
                  .collect(Collectors.toList()) : null;
              questionDetail.setMatchingPairs(pairs);
            }

            questionDetail.setMatchingColumns(columns);
          }

          return questionDetail;
        })
        .collect(Collectors.toList());

    response.setAnswers(answers);
    response.setQuestions(questions);

    if (showRightAnswer) {
      Map<String, Object> correctAnswers = new HashMap<>();
      for (QuestionDetailResponse question : questions) {
        Object correctAnswer = questionService.getCorrectAnswerFormatted(question);
        correctAnswers.put(question.getId(), correctAnswer);
      }
      response.setCorrectAnswers(correctAnswers);
    }

    return response;
  }

  public List<ExamAttemptListResponse> getAttemptsByExamId(String examId, User user) {
    List<ExamAttempt> attempts;
    if (user.getRole() == 1) {
      attempts = examAttemptRepository.findByExamId(examId);
    } else {
      attempts = examAttemptRepository.findByExamIdAndUserId(examId, user.getId());
    }

    return attempts.stream().filter(attempt -> {
      if (attempt.getStatus().equals("in_progress")) {
        // Kiểm tra thời gian để tự động nộp bài
        Date now = new Date();
        long timeElapsed = now.getTime() - attempt.getStartTime().getTime();
        long timeElapsedMinutes = timeElapsed / (60 * 1000);

        if (timeElapsedMinutes >= attempt.getDuration()) {
          attempt.setStatus("submitted");
          attempt.setEndTime(new Date(attempt.getStartTime().getTime() + (long) attempt.getDuration() * 60 * 1000));
          attempt.setScore(calculateScore(attempt.getId()));

          examAttemptRepository.save(attempt);

          return true;
        } else {
          // Chưa vượt quá thời gian, loại bỏ khỏi kết quả
          return false;
        }
      }

      return "submitted".equals(attempt.getStatus());
    }).map(attempt -> {
      ExamAttemptListResponse response = new ExamAttemptListResponse();
      response.setId(attempt.getId());
      response.setTitle(attempt.getTitle());
      response.setScore(attempt.getScore());
      response.setMaxScore(attempt.getMaxScore());
      response.setStartTime(attempt.getStartTime());
      response.setEndTime(attempt.getEndTime());
      response.setStatus(attempt.getStatus());
      response.setClassId(attempt.getClassId());
      response.setClassName(courseService.getById(attempt.getClassId()).get().getName());
      response.setUserId(attempt.getUserId());

      // Nếu đây là 1 request từ học sinh, thì vì học sinh đó chỉ xem được kết quả của
      // mình nên tận dụng biến User luôn.
      if (user.getRole() == 0) {
        response.setStudentName(user.getName());
        response.setStudentAvatar(user.getAvatar());
        response.setStudentSchool(user.getSchool());
        response.setStudentGrade(user.getGrade());
      } else {
        // Lấy thông tin học sinh
        User student = userRepository.findById(attempt.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        response.setStudentName(student.getName());
        response.setStudentAvatar(student.getAvatar());
        response.setStudentSchool(student.getSchool());
        response.setStudentGrade(student.getGrade());
      }

      return response;
    }).collect(Collectors.toList());
  }
}