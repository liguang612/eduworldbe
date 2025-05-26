package com.example.eduworldbe.dto;

import com.example.eduworldbe.model.Review;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.model.ReviewComment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
  private String id;
  private String userId;
  private Integer targetType;
  private String targetId;
  private int score;
  private String comment;
  private Date createdAt;

  // Thông tin người dùng
  private String userName;
  private String userAvatar;
  private String userSchool;
  private Integer userGrade;

  // Danh sách comment
  private List<ReviewCommentResponse> comments;

  public static ReviewResponse fromReviewAndUser(Review review, User user, List<ReviewCommentResponse> comments) {
    ReviewResponse response = new ReviewResponse();
    response.setId(review.getId());
    response.setUserId(review.getUserId());
    response.setTargetType(review.getTargetType());
    response.setTargetId(review.getTargetId());
    response.setScore(review.getScore());
    response.setComment(review.getComment());
    response.setCreatedAt(review.getCreatedAt());
    response.setComments(comments);

    if (user != null) {
      response.setUserName(user.getName());
      response.setUserAvatar(user.getAvatar());
      response.setUserSchool(user.getSchool());
      response.setUserGrade(user.getGrade());
    }

    return response;
  }
}