package com.example.eduworldbe.dto;

public class AddMemberRequest {
  private String userId;
  private Integer role;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Integer getRole() {
    return role;
  }

  public void setRole(Integer role) {
    this.role = role;
  }
}