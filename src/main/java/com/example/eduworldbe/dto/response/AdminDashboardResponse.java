package com.example.eduworldbe.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
  private DashboardStats stats;
  private List<MonthlyUserData> monthlyUserChart;
  private List<DailyUserData> dailyUserChart;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DashboardStats {
    private Long totalTeachers;
    private Long totalStudents;
    private Long todayActiveUsers;
    private Long todayLogins;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MonthlyUserData {
    private String month; // Format: "YYYY-MM"
    private Long teacherCount;
    private Long studentCount;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class DailyUserData {
    private String date; // Format: "YYYY-MM-DD"
    private Long teacherCount;
    private Long studentCount;
  }
}