package com.example.eduworldbe.dto;

import com.example.eduworldbe.model.Choice;
import com.example.eduworldbe.model.MatchingColumn;
import com.example.eduworldbe.model.MatchingPair;
import com.example.eduworldbe.model.Question;
import com.example.eduworldbe.model.SharedMedia;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailResponse {
  private String id;
  private String title;
  private String subjectId;
  private String type; // radio, checkbox, itemConnector, ranking, shortAnswer
  private SharedMedia sharedMedia;
  private Integer level;
  private String createdBy;
  private List<String> categories;
  private List<String> solutionIds;
  private List<String> reviewIds;
  private Date createdAt;
  private Date updatedAt;
  private List<Choice> choices; // List of choices for radio/checkbox types
  private List<MatchingColumn> matchingColumns; // List of columns for matching type
  private List<MatchingPair> matchingPairs; // List of pairs for matching type

  // Constructor to build from Question model
  public QuestionDetailResponse(Question question) {
    this.id = question.getId();
    this.title = question.getTitle();
    this.subjectId = question.getSubjectId();
    this.type = question.getType();
    this.sharedMedia = question.getSharedMedia();
    this.level = question.getLevel();
    this.createdBy = question.getCreatedBy();
    this.categories = question.getCategories();
    this.solutionIds = question.getSolutionIds();
    this.reviewIds = question.getReviewIds();
    this.createdAt = question.getCreatedAt();
    this.updatedAt = question.getUpdatedAt();

    this.choices = null;
    this.matchingColumns = null;
    this.matchingPairs = null;
  }
}