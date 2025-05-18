package com.example.eduworldbe.dto;

import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.model.MatchingPair;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionListResponseItem {
  private String id;
  private String title;
  private String subjectId;
  private String type; // radio, checkbox, itemConnector, ordering, shortAnswer
  private Integer level;
  private String createdBy;
  private List<String> categories;
  private List<String> solutionIds;
  private List<String> reviewIds;
  private Date createdAt;
  private Date updatedAt;

  // Additional fields for question details
  private List<Choice> choices;
  private List<MatchingColumn> matchingColumns;
  private List<MatchingPair> matchingPairs;

  // Constructor to build from Question model
  public QuestionListResponseItem(Question question) {
    this.id = question.getId();
    this.title = question.getTitle();
    this.subjectId = question.getSubjectId();
    this.type = question.getType();
    this.level = question.getLevel();
    this.createdBy = question.getCreatedBy();
    this.categories = question.getCategories();
    this.solutionIds = question.getSolutionIds();
    this.reviewIds = question.getReviewIds();
    this.createdAt = question.getCreatedAt();
    this.updatedAt = question.getUpdatedAt();
  }
}