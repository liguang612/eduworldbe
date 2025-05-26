package com.example.eduworldbe.dto;

import com.example.eduworldbe.model.ReviewComment;
import com.example.eduworldbe.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentResponse {
  private String id;
  private String reviewId;
  private String userId;
  private String content;
  private Date createdAt;
  private Date updatedAt;

  // Thông tin người comment
  private String userName;
  private String userAvatar;
  private String userSchool;
  private Integer userGrade;

  public static ReviewCommentResponse fromCommentAndUser(ReviewComment comment, User user) {
    ReviewCommentResponse response = new ReviewCommentResponse();
    response.setId(comment.getId());
    response.setReviewId(comment.getReviewId());
    response.setUserId(comment.getUserId());
    response.setContent(comment.getContent());
    response.setCreatedAt(comment.getCreatedAt());
    response.setUpdatedAt(comment.getUpdatedAt());

    if (user != null) {
      response.setUserName(user.getName());
      response.setUserAvatar(user.getAvatar());
      response.setUserSchool(user.getSchool());
      response.setUserGrade(user.getGrade());
    }

    return response;
  }
}