package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyLoginResponse {
  private String name;
  private String avatar;
  private String email;
  private Date loginTime;
  private String ipAddress;
  private Integer role;
}