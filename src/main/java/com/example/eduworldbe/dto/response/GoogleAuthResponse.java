package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleAuthResponse {
  private String accessToken;
  private String refreshToken;
  private boolean isNewUser;
  private UserInfo userInfo;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class UserInfo {
    private String id;
    private String email;
    private String fullName;
    private String avatar;
    private Integer role;
  }
}