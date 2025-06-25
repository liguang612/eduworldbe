package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.response.AdminDashboardResponse;
import com.example.eduworldbe.dto.response.StorageUsageResponse;
import com.example.eduworldbe.model.StorageUsage;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.LoginActivityRepository;
import com.example.eduworldbe.repository.StorageUsageRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AdminService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private LoginActivityRepository loginActivityRepository;

  @Autowired
  private StorageUsageRepository storageUsageRepository;

  public AdminDashboardResponse getDashboardData() {
    Date today = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    calendar.add(Calendar.DAY_OF_MONTH, -30); // 30 ngày trước
    Date thirtyDaysAgo = calendar.getTime();

    // Lấy thống kê tổng quan
    Long totalTeachers = userRepository.countByRole(1);
    Long totalStudents = userRepository.countByRole(0);
    Long todayActiveUsers = loginActivityRepository.countUniqueUsersByDate(today);
    Long todayLogins = loginActivityRepository.countByLoginTimeBetween(
        getStartOfDay(today), getEndOfDay(today));

    // Lấy dữ liệu biểu đồ hàng tháng
    List<AdminDashboardResponse.MonthlyUserData> monthlyData = getMonthlyUserData();

    // Lấy dữ liệu biểu đồ hàng ngày
    List<AdminDashboardResponse.DailyUserData> dailyData = getDailyUserData(thirtyDaysAgo, today);

    return AdminDashboardResponse.builder()
        .stats(AdminDashboardResponse.DashboardStats.builder()
            .totalTeachers(totalTeachers)
            .totalStudents(totalStudents)
            .todayActiveUsers(todayActiveUsers)
            .todayLogins(todayLogins)
            .build())
        .monthlyUserChart(monthlyData)
        .dailyUserChart(dailyData)
        .build();
  }

  public StorageUsageResponse getStorageUsageData() {
    Long totalStorageUsed = storageUsageRepository.getTotalStorageUsed();
    if (totalStorageUsed == null)
      totalStorageUsed = 0L;

    List<StorageUsageResponse.UserStorageData> userData = getUserStorageData();
    List<StorageUsageResponse.FileTypeStorageData> fileTypeData = getFileTypeStorageData();

    return StorageUsageResponse.builder()
        .totalStorageUsed(totalStorageUsed)
        .userStorageData(userData)
        .fileTypeStorageData(fileTypeData)
        .build();
  }

  private List<AdminDashboardResponse.MonthlyUserData> getMonthlyUserData() {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MONTH, -12); // 12 tháng trước
    Date twelveMonthsAgo = calendar.getTime();
    Date now = new Date();

    List<Object[]> monthlyCountsByRole = userRepository.getMonthlyUserCountsByRole(twelveMonthsAgo, now);

    Map<String, AdminDashboardResponse.MonthlyUserData> monthlyDataMap = new HashMap<>();

    for (Object[] row : monthlyCountsByRole) {
      Integer year = (Integer) row[0];
      Integer month = (Integer) row[1];
      Integer role = (Integer) row[2];
      Long count = (Long) row[3];

      String monthStr = String.format("%04d-%02d", year, month);

      monthlyDataMap.computeIfAbsent(monthStr, k -> AdminDashboardResponse.MonthlyUserData.builder()
          .month(monthStr)
          .teacherCount(0L)
          .studentCount(0L)
          .build());

      AdminDashboardResponse.MonthlyUserData data = monthlyDataMap.get(monthStr);
      if (role == 1) { // Teacher
        data.setTeacherCount(count);
      } else { // Student
        data.setStudentCount(count);
      }
    }

    return monthlyDataMap.values().stream()
        .sorted(Comparator.comparing(AdminDashboardResponse.MonthlyUserData::getMonth))
        .collect(Collectors.toList());
  }

  private List<AdminDashboardResponse.DailyUserData> getDailyUserData(Date startDate, Date endDate) {
    List<Object[]> dailyCountsByRole = loginActivityRepository.getDailyUserCountsByRole(startDate, endDate);

    Map<String, AdminDashboardResponse.DailyUserData> dailyDataMap = new HashMap<>();

    for (Object[] row : dailyCountsByRole) {
      Date date = (Date) row[0];
      Integer role = (Integer) row[1];
      Long count = (Long) row[2];

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      String dateStr = sdf.format(date);

      dailyDataMap.computeIfAbsent(dateStr, k -> AdminDashboardResponse.DailyUserData.builder()
          .date(dateStr)
          .teacherCount(0L)
          .studentCount(0L)
          .build());

      AdminDashboardResponse.DailyUserData data = dailyDataMap.get(dateStr);
      if (role == 1) { // Teacher
        data.setTeacherCount(count);
      } else { // Student
        data.setStudentCount(count);
      }
    }

    return dailyDataMap.values().stream()
        .sorted(Comparator.comparing(AdminDashboardResponse.DailyUserData::getDate))
        .collect(Collectors.toList());
  }

  private List<StorageUsageResponse.UserStorageData> getUserStorageData() {
    List<Object[]> userStorageData = storageUsageRepository.getStorageUsageByUser();

    return userStorageData.stream()
        .map(row -> {
          String userId = (String) row[0];
          Long totalSize = (Long) row[1];
          Long fileCount = (Long) row[2];

          // Lấy email từ User table
          User user = userRepository.findById(userId).orElse(null);
          String userEmail = user != null ? user.getEmail() : "Unknown";

          return StorageUsageResponse.UserStorageData.builder()
              .userId(userId)
              .userEmail(userEmail)
              .totalSize(totalSize)
              .fileCount(fileCount)
              .formattedSize(formatFileSize(totalSize))
              .build();
        })
        .collect(Collectors.toList());
  }

  private List<StorageUsageResponse.FileTypeStorageData> getFileTypeStorageData() {
    List<Object[]> fileTypeStorageData = storageUsageRepository.getStorageUsageByFileType();

    return fileTypeStorageData.stream()
        .map(row -> {
          String fileType = (String) row[0];
          Long totalSize = (Long) row[1];
          Long fileCount = (Long) row[2];

          return StorageUsageResponse.FileTypeStorageData.builder()
              .fileType(fileType)
              .totalSize(totalSize)
              .fileCount(fileCount)
              .formattedSize(formatFileSize(totalSize))
              .build();
        })
        .collect(Collectors.toList());
  }

  private Date getStartOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    return calendar.getTime();
  }

  private Date getEndOfDay(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    calendar.set(Calendar.MILLISECOND, 999);
    return calendar.getTime();
  }

  private String formatFileSize(Long bytes) {
    if (bytes == null || bytes == 0)
      return "0 B";

    String[] units = { "B", "KB", "MB", "GB", "TB" };
    int unitIndex = 0;
    double size = bytes;

    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }

    return String.format("%.2f %s", size, units[unitIndex]);
  }

  public StorageUsage createNewRecord(StorageUsage storageUsage) {
    storageUsage.setUploadTime(new Date());

    long totalBytes = 0L;
    try {
      Storage storage = StorageClient.getInstance().bucket().getStorage();
      String bucketName = StorageClient.getInstance().bucket().getName();

      String firebasePath = extractFirebasePath(storageUsage.getFileUrl());
      Blob blob = storage.get(bucketName, firebasePath);
      if (blob != null && blob.exists()) {
        totalBytes += blob.getSize();
      }

      String fileName = getFileNameFromPath(firebasePath);
      storageUsage.setFileName(fileName);
    } catch (Exception e) {
      System.out.println("Lỗi khi tính toán kích thước tổng cộng: " + e.getMessage());
    }

    storageUsage.setFileSize(totalBytes);
    return storageUsageRepository.save(storageUsage);
  }

  public static String extractFirebasePath(String url) {
    Pattern pattern = Pattern.compile("/o/([^?]+)");
    Matcher matcher = pattern.matcher(url);

    if (matcher.find()) {
      String encodedPath = matcher.group(1);
      return URLDecoder.decode(encodedPath, StandardCharsets.UTF_8);
    }
    return null;
  }

  public static String getFileNameFromPath(String path) {
    if (path == null || path.isEmpty())
      return null;
    int lastSlashIndex = path.lastIndexOf('/');
    if (lastSlashIndex >= 0 && lastSlashIndex < path.length() - 1) {
      return path.substring(lastSlashIndex + 1);
    }
    return null;
  }
}