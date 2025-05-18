package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.model.SharedMedia;
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

@Service
public class QuestionService {
  @Autowired
  private QuestionRepository questionRepository;

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
        List<Choice> choices = choiceService.getByQuestionId(id);
        choices.forEach(choice -> {
          if (!isCreator) {
            choice.setIsCorrect(null);
            choice.setOrderIndex(null);
            choice.setTextAnswer(null);
          }
        });
        detailResponse.setChoices(choices);
        break;
      case "itemConnector":
        List<MatchingColumn> matchingColumns = matchingColumnService.getByQuestionId(id);
        if (isCreator) {
          List<MatchingPair> matchingPairs = matchingPairService.getByQuestionId(id);
          detailResponse.setMatchingPairs(matchingPairs);
        }
        detailResponse.setMatchingColumns(matchingColumns);
        break;
      case "shortAnswer":
        List<Choice> shortAnswerChoices = choiceService.getByQuestionId(id);
        if (!isCreator) {
          shortAnswerChoices.forEach(choice -> {
            choice.setTextAnswer(null);
          });
        }
        detailResponse.setChoices(shortAnswerChoices);
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
                  choice.setTextAnswer(null);
                });
              } else {
                choices.forEach(choice -> {
                  choice.setTextAnswer(null);
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
      Integer usageCount = sharedMedia.getUsageCount();

      sharedMediaService.decrementUsageCount(mediaId);

      // If this was the last question using this SharedMedia, delete the SharedMedia
      if (usageCount - 1 == 0) {
        sharedMediaService.delete(mediaId);
      }
    }

    questionRepository.deleteById(id);
  }
}