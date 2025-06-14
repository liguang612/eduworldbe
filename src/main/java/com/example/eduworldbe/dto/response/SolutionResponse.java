package com.example.eduworldbe.dto.response;

import com.example.eduworldbe.model.Solution;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolutionResponse {
  private String id;
  private String questionId;
  private String content;
  private Integer status; // 0: PENDING, 1: APPROVED, 2: REJECTED
  private String reviewedBy;
  private Date reviewedAt;
  private Date createdAt;

  // Thông tin người tạo
  private String creatorId;
  private String creatorName;
  private String creatorSchool;
  private Integer creatorGrade;
  private String creatorAvatar;
  private Integer creatorRole;

  public static SolutionResponse fromSolution(Solution solution, String creatorName, String creatorSchool,
      Integer creatorGrade, String creatorAvatar, Integer creatorRole) {
    SolutionResponse response = new SolutionResponse();
    response.setId(solution.getId());
    response.setQuestionId(solution.getQuestionId());
    response.setContent(solution.getContent());
    response.setStatus(solution.getStatus());
    response.setReviewedBy(solution.getReviewedBy());
    response.setReviewedAt(solution.getReviewedAt());
    response.setCreatedAt(solution.getCreatedAt());
    response.setCreatorId(solution.getCreatedBy());
    response.setCreatorName(creatorName);
    response.setCreatorSchool(creatorSchool);
    response.setCreatorGrade(creatorGrade);
    response.setCreatorAvatar(creatorAvatar);
    response.setCreatorRole(creatorRole);
    return response;
  }
}