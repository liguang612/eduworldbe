package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDetailResponse {
  private String id; // Login Activity ID
  private Date loginTime;
  private String loginMethod;
  private String ipAddress;
  private String userAgent;
  private UserInfo user;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserInfo {
    private String id;
    private String name;
    private String avatar;
    private String email;
    private String school;
    private Integer grade;
    private Integer role;
  }
}