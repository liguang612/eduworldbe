package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.model.SharedMedia;
import com.example.eduworldbe.repository.ChoiceRepository;
import com.example.eduworldbe.repository.QuestionRepository;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.dto.QuestionDetailResponse;
import com.example.eduworldbe.dto.CreateQuestionRequest;
import com.example.eduworldbe.dto.QuestionListResponseItem;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.AbstractMap;
import java.util.ArrayList;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;

@Service
public class QuestionService {
  @Autowired
  private QuestionRepository questionRepository;

  @Autowired
  private ChoiceRepository choiceRepository;
  @Autowired
  private AuthUtil authUtil;

  @Autowired
  private ChoiceService choiceService;

  @Autowired
  private MatchingColumnService matchingColumnService;

  @Autowired
  private MatchingPairService matchingPairService;

  @Autowired
  private SharedMediaService sharedMediaService;

  @Autowired
  private ObjectMapper objectMapper;

  public Question create(CreateQuestionRequest request, HttpServletRequest httpRequest) {
    User user = authUtil.getCurrentUser(httpRequest);
    if (user == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
    }

    Question question = new Question();
    question.setTitle(request.getTitle());
    question.setSubjectId(request.getSubjectId());
    question.setType(request.getType());
    question.setLevel(request.getLevel());
    question.setCategories(request.getCategories());

    // Xử lý SharedMedia
    if (request.getSharedMediaId() != null) {
      SharedMedia sharedMedia = sharedMediaService.getById(request.getSharedMediaId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SharedMedia not found"));
      question.setSharedMedia(sharedMedia);
      sharedMediaService.incrementUsageCount(sharedMedia.getId());
    }

    question.setCreatedBy(user.getId());
    question.setCreatedAt(new Date());
    question.setUpdatedAt(new Date());

    return questionRepository.save(question);
  }

  public Optional<Question> getById(String id) {
    return questionRepository.findById(id);
  }

  public Optional<QuestionDetailResponse> getQuestionDetailById(String id, HttpServletRequest request) {
    Optional<Question> questionOptional = questionRepository.findWithSharedMediaById(id);
    if (questionOptional.isEmpty()) {
      return Optional.empty();
    }

    Question question = questionOptional.get();
    QuestionDetailResponse detailResponse = new QuestionDetailResponse(question);

    // Get current user from token
    User currentUser = authUtil.getCurrentUser(request);
    boolean isCreator = currentUser != null && currentUser.getId().equals(question.getCreatedBy());

    switch (question.getType()) {
      case "radio":
      case "checkbox":
      case "ranking":
        List<Choice> fetchedChoices = choiceService.getByQuestionId(id);
        if (!isCreator) {
          // Case study: Tại sao lại phải tạo dãy đối tượng mới như này? -> Tránh cache
          // của hibernate.
          List<Choice> choicesForResponse = new ArrayList<>();
          for (Choice fetchedChoice : fetchedChoices) {
            Choice responseChoice = new Choice();

            responseChoice.setId(fetchedChoice.getId());
            responseChoice.setQuestionId(fetchedChoice.getQuestionId());
            responseChoice.setText(fetchedChoice.getText());
            responseChoice.setValue(fetchedChoice.getValue());
            responseChoice.setIsCorrect(null);
            responseChoice.setOrderIndex(null);

            choicesForResponse.add(responseChoice);
          }
          detailResponse.setChoices(choicesForResponse);
        } else {
          detailResponse.setChoices(fetchedChoices);
        }
        break;
      case "itemConnector":
        List<MatchingColumn> matchingColumns = matchingColumnService.getByQuestionId(id);
        if (isCreator) {
          List<MatchingPair> matchingPairs = matchingPairService.getByQuestionId(id);
          detailResponse.setMatchingPairs(matchingPairs);
        } else {
          List<MatchingColumn> columnsForResponse = new ArrayList<>();
          for (MatchingColumn col : matchingColumns) {
            MatchingColumn responseCol = new MatchingColumn();

            responseCol.setId(col.getId());
            responseCol.setQuestionId(col.getQuestionId());
            responseCol.setLabel(col.getLabel());
            responseCol.setOrderIndex(null);
            columnsForResponse.add(responseCol);
          }
          detailResponse.setMatchingColumns(columnsForResponse);
        }
        detailResponse.setMatchingColumns(matchingColumns);
        break;
      case "shortAnswer":
        List<Choice> shortAnswerChoicesFetched = choiceService.getByQuestionId(id);
        if (!isCreator) {
          List<Choice> choicesForResponse = new ArrayList<>();
          for (Choice fetchedChoice : shortAnswerChoicesFetched) {
            Choice responseChoice = new Choice();

            responseChoice.setId(fetchedChoice.getId());
            responseChoice.setQuestionId(fetchedChoice.getQuestionId());
            responseChoice.setText(fetchedChoice.getText());
            responseChoice.setValue(null);
            responseChoice.setIsCorrect(null);
            responseChoice.setOrderIndex(null);

            choicesForResponse.add(responseChoice);
          }
          detailResponse.setChoices(choicesForResponse);
        } else {
          detailResponse.setChoices(shortAnswerChoicesFetched);
        }
        break;
    }

    return Optional.of(detailResponse);
  }

  public List<Question> getAll() {
    return questionRepository.findAll();
  }

  public List<Question> getByType(String type) {
    return questionRepository.findByType(type);
  }

  public List<Question> getByCreatedBy(String createdBy) {
    return questionRepository.findByCreatedBy(createdBy);
  }

  public List<Question> getBySubjectId(String subjectId) {
    return questionRepository.findBySubjectId(subjectId);
  }

  public List<QuestionListResponseItem> getBySharedMediaId(String sharedMediaId, HttpServletRequest request) {
    // Get current user from token
    User currentUser = authUtil.getCurrentUser(request);

    return questionRepository.findBySharedMediaId(sharedMediaId).stream()
        .map(question -> {
          QuestionListResponseItem item = new QuestionListResponseItem(question);

          boolean isCreator = currentUser != null && currentUser.getId().equals(question.getCreatedBy());

          if (Arrays.asList("radio", "checkbox", "ranking", "shortAnswer").contains(question.getType())) {
            List<Choice> choices = choiceService.getByQuestionId(question.getId());

            if (!isCreator) {
              if (!"shortAnswer".equals(question.getType())) {
                choices.forEach(choice -> {
                  choice.setIsCorrect(null);
                  choice.setOrderIndex(null);
                });
              } else {
                choices.forEach(choice -> {
                  choice.setValue(null);
                });
              }
            }

            item.setChoices(choices);
          }

          // Get matching columns and pairs for itemConnector type
          if ("itemConnector".equals(question.getType())) {
            List<MatchingColumn> matchingColumns = matchingColumnService.getByQuestionId(question.getId());
            List<MatchingPair> matchingPairs = matchingPairService.getByQuestionId(question.getId());

            if (!isCreator) {
              matchingColumns.forEach(column -> {
                column.setOrderIndex(null);
              });
              matchingPairs.forEach(pair -> {
                // You might need to add fields to MatchingPair to hide here if needed
              });
            }

            item.setMatchingColumns(matchingColumns);
            item.setMatchingPairs(matchingPairs);
          }

          return item;
        })
        .collect(Collectors.toList());
  }

  public List<Question> getAllFiltered(String createdBy, String subjectId) {
    if (createdBy != null && !createdBy.isEmpty() && subjectId != null && !subjectId.isEmpty()) {
      return questionRepository.findByCreatedByAndSubjectId(createdBy, subjectId);
    } else if (createdBy != null && !createdBy.isEmpty()) {
      return questionRepository.findByCreatedBy(createdBy);
    } else if (subjectId != null && !subjectId.isEmpty()) {
      return questionRepository.findBySubjectId(subjectId);
    } else {
      return questionRepository.findAll();
    }
  }

  @Transactional
  public Question update(String id, Question updated) {
    Question existing = questionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

    // Update basic fields
    if (updated.getTitle() != null)
      existing.setTitle(updated.getTitle());
    if (updated.getSubjectId() != null)
      existing.setSubjectId(updated.getSubjectId());
    if (updated.getType() != null)
      existing.setType(updated.getType());
    if (updated.getSharedMedia() != null)
      existing.setSharedMedia(updated.getSharedMedia());
    if (updated.getLevel() != null)
      existing.setLevel(updated.getLevel());
    if (updated.getCategories() != null)
      existing.setCategories(updated.getCategories());
    if (updated.getSolutionIds() != null)
      existing.setSolutionIds(updated.getSolutionIds());
    if (updated.getReviewIds() != null)
      existing.setReviewIds(updated.getReviewIds());
    existing.setUpdatedAt(new Date());

    // Delete all existing choices
    choiceService.deleteByQuestionId(id);

    // Delete all existing matching columns and pairs
    matchingColumnService.deleteByQuestionId(id);
    matchingPairService.deleteByQuestionId(id);

    return questionRepository.save(existing);
  }

  @Transactional
  public void delete(String id) {
    Question question = questionRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

    // Handle SharedMedia if exists
    if (question.getSharedMedia() != null) {
      SharedMedia sharedMedia = question.getSharedMedia();
      String mediaId = sharedMedia.getId();

      sharedMediaService.decrementUsageCount(mediaId);
    }

    questionRepository.deleteById(id);
  }

  public List<QuestionDetailResponse> getQuestionDetailsByIds(List<String> ids, HttpServletRequest request) {
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    }

    return ids.stream()
        .map(id -> {
          try {
            return getQuestionDetailById(id, request);
          } catch (Exception e) {
            // Log lỗi để kiểm tra
            System.out.println("Lỗi khi lấy chi tiết câu hỏi ID: " + id + " - " + e.getMessage());
            return Optional.<QuestionDetailResponse>empty();
          }
        })
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }

  private int calculateLevenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];

    for (int i = 0; i <= s1.length(); i++) {
      dp[i][0] = i;
    }
    for (int j = 0; j <= s2.length(); j++) {
      dp[0][j] = j;
    }

    for (int i = 1; i <= s1.length(); i++) {
      for (int j = 1; j <= s2.length(); j++) {
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
        }
      }
    }

    return dp[s1.length()][s2.length()];
  }

  public List<Question> searchQuestions(List<Question> questions, String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return questions;
    }

    String[] searchTerms = keyword.toLowerCase().split("\\s+");

    return questions.stream()
        .map(question -> {
          String questionTitle = question.getTitle().toLowerCase();
          List<String> categories = question.getCategories() != null
              ? question.getCategories().stream()
                  .map(String::toLowerCase)
                  .toList()
              : List.of();

          double score = 0.0;

          // Tính điểm cho mỗi từ khóa
          for (String term : searchTerms) {
            double termScore = 0.0;
            boolean isCategorySearch = term.startsWith("#");
            String searchTerm = isCategorySearch ? term.substring(1) : term;

            if (isCategorySearch) {
              for (String category : categories) {
                // Khớp chính xác
                if (category.equals(searchTerm)) {
                  termScore += 100.0;
                }
                // Khớp một phần
                else if (category.contains(searchTerm)) {
                  double lengthRatio = (double) searchTerm.length() / category.length();
                  termScore += 80.0 * lengthRatio;
                }

                // Levenshtein distance
                int distance = calculateLevenshteinDistance(category, searchTerm);
                if (distance <= 4) {
                  termScore += Math.max(0, 30.0 * (1 - distance / 3.0));
                }
              }
            } else {
              // Khớp chính xác
              if (questionTitle.equals(searchTerm)) {
                termScore += 100.0;
              }
              // Khớp một phần
              else if (questionTitle.contains(searchTerm)) {
                double lengthRatio = (double) searchTerm.length() / questionTitle.length();
                termScore += 80.0 * lengthRatio;
              }

              // Levenshtein distance
              int distance = calculateLevenshteinDistance(questionTitle, searchTerm);
              if (distance <= 3) {
                termScore += Math.max(0, 30.0 * (1 - distance / 3.0));
              }

              // Kiểm tra Levenshtein distance cho từng từ trong title
              String[] titleWords = questionTitle.split("\\s+");
              for (String word : titleWords) {
                distance = calculateLevenshteinDistance(word, searchTerm);
                if (distance <= 2) {
                  termScore += Math.max(0, 20.0 * (1 - distance / 2.0));
                }
              }
            }

            score += termScore;
          }

          score = score / searchTerms.length;

          return new AbstractMap.SimpleEntry<>(question, score);
        })
        .filter(entry -> entry.getValue() > 0)
        .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue())) // Sắp xếp theo điểm giảm dần
        .map(AbstractMap.SimpleEntry::getKey)
        .toList();
  }

  public boolean checkAnswer(QuestionDetailResponse question, Object userAnswer) {
    if (question == null || userAnswer == null) {
      return false;
    }

    try {
      switch (question.getType()) {
        case "radio": {
          List<Choice> choices = choiceService.getByQuestionId(question.getId());
          Optional<Choice> correctChoice = choices.stream()
              .filter(Choice::getIsCorrect)
              .findFirst();
          return correctChoice.isPresent() && correctChoice.get().getValue().equals(userAnswer);
        }
        case "checkbox": {
          List<Choice> choices = choiceRepository.findByQuestionId(question.getId());
          List<String> correctValues = choices.stream()
              .filter(Choice::getIsCorrect)
              .map(Choice::getValue)
              .sorted()
              .toList();
          List<String> userValues = objectMapper.readValue(objectMapper.writeValueAsString(userAnswer),
              new TypeReference<List<String>>() {
              });
          userValues.sort(String::compareTo);
          return correctValues.equals(userValues);
        }
        case "shortAnswer": {
          List<Choice> choices = choiceRepository.findByQuestionId(question.getId());
          if (choices.isEmpty())
            return false;
          String correctAnswer = choices.get(0).getValue().toLowerCase().replace(".", "");
          String userAnswerStr = userAnswer.toString().toLowerCase().replace(".", "");
          return correctAnswer.equals(userAnswerStr);
        }
        case "ranking": {
          List<Choice> choices = choiceRepository.findByQuestionId(question.getId());
          List<String> correctOrder = choices.stream()
              .sorted(Comparator.comparing(Choice::getOrderIndex))
              .map(Choice::getValue)
              .toList();
          List<String> userOrder = objectMapper.readValue(objectMapper.writeValueAsString(userAnswer),
              new TypeReference<List<String>>() {
              });
          return correctOrder.equals(userOrder);
        }
        case "itemConnector": {
          List<MatchingPair> correctPairs = matchingPairService.getByQuestionId(question.getId());
          List<Map<String, String>> userPairs = objectMapper.readValue(objectMapper.writeValueAsString(userAnswer),
              new TypeReference<List<Map<String, String>>>() {
              });

          if (correctPairs.size() != userPairs.size())
            return false;

          return correctPairs.stream().allMatch(correctPair -> userPairs.stream().anyMatch(
              userPair -> userPair.get("from").equals(correctPair.getFrom()) &&
                  userPair.get("to").equals(correctPair.getTo())));
        }
        default:
          return false;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  public Object getCorrectAnswerFormatted(QuestionDetailResponse question) {
    if (question == null || question.getType() == null) {
      return null;
    }

    switch (question.getType()) {
      case "radio": {
        List<Choice> choices = choiceService.getByQuestionId(question.getId());
        if (choices != null && !choices.isEmpty()) {
          Optional<Choice> correctChoice = choices.stream()
              .filter(Choice::getIsCorrect)
              .findFirst();
          return correctChoice.map(Choice::getValue).orElse(null);
        }
        return null;
      }
      case "shortAnswer": {
        List<Choice> choices = choiceService.getByQuestionId(question.getId());
        if (choices != null && !choices.isEmpty()) {
          Optional<Choice> correctChoice = choices.stream()
              .findFirst();
          return correctChoice.map(Choice::getValue).orElse(null);
        }
        return null;
      }
      case "checkbox": {
        List<Choice> choices = choiceService.getByQuestionId(question.getId());
        if (choices != null && !choices.isEmpty()) {
          return choices.stream()
              .filter(Choice::getIsCorrect)
              .map(Choice::getValue)
              .toList();
        }
        return new ArrayList<>();
      }
      case "ranking": {
        List<Choice> choices = choiceService.getByQuestionId(question.getId());
        if (choices != null && !choices.isEmpty()) {
          return choices.stream()
              .sorted(Comparator.comparing(Choice::getOrderIndex))
              .map(Choice::getValue)
              .toList();
        }
        return new ArrayList<>();
      }
      case "itemConnector": {
        List<MatchingPair> pairs = matchingPairService.getByQuestionId(question.getId());
        if (pairs != null && !pairs.isEmpty()) {
          return pairs.stream()
              .map(pair -> {
                Map<String, String> map = new HashMap<>();
                map.put("from", pair.getFrom());
                map.put("to", pair.getTo());
                return map;
              })
              .toList();
        }
        return new ArrayList<>();
      }
      default:
        return null;
    }
  }
}