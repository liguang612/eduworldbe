package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.request.TeacherSearchRequest;
import com.example.eduworldbe.dto.response.TeacherStorageListResponse;
import com.example.eduworldbe.dto.response.TeacherStorageResponse;
import com.example.eduworldbe.dto.response.UserFileDetailResponse;
import com.example.eduworldbe.dto.response.UserStorageInfoResponse;
import com.example.eduworldbe.model.StorageUsage;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.StorageUsageRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorageUsageService {

  @Autowired
  private StorageUsageRepository storageUsageRepository;

  @Autowired
  private UserRepository userRepository;

  public void recordFileUpload(String userId, String fileName, String fileUrl,
      Long fileSize, String fileType) {
    StorageUsage storageUsage = new StorageUsage();
    storageUsage.setUserId(userId);
    storageUsage.setFileName(fileName);
    storageUsage.setFileUrl(fileUrl);
    storageUsage.setFileSize(fileSize);
    storageUsage.setFileType(fileType);
    storageUsage.setUploadTime(new Date());
    storageUsageRepository.save(storageUsage);
  }

  public Long getTotalStorageUsedByUser(String userId) {
    Long totalSize = storageUsageRepository.getTotalStorageUsedByUser(userId);
    return totalSize != null ? totalSize : 0L;
  }

  public Long getTotalStorageUsed() {
    Long totalSize = storageUsageRepository.getTotalStorageUsed();
    return totalSize != null ? totalSize : 0L;
  }

  public TeacherStorageListResponse getTeacherStorageList(TeacherSearchRequest request) {
    List<Object[]> storageData = storageUsageRepository.getStorageUsageByUser();

    List<TeacherStorageResponse> teachers = storageData.stream()
        .map(data -> {
          String userId = (String) data[0];
          Long totalSize = (Long) data[1];
          Integer fileCount = ((Number) data[2]).intValue();

          User user = userRepository.findById(userId).orElse(null);
          if (user != null && user.getRole() == 1) {
            return new TeacherStorageResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAvatar(),
                user.getBirthday(),
                totalSize,
                fileCount,
                null,
                user.getStorageLimit());
          }
          return null;
        })
        .filter(response -> response != null)
        .collect(Collectors.toList());

    if (request.hasSearchCriteria()) {
      teachers = applyFuzzySearch(teachers, request);

      return TeacherStorageListResponse.builder()
          .teachers(teachers)
          .totalPages(1)
          .totalElements((long) teachers.size())
          .currentPage(0)
          .pageSize(teachers.size())
          .build();
    } else {
      // Khi không tìm kiếm, phân trang bình thường
      int totalElements = teachers.size();
      int totalPages = (int) Math.ceil((double) totalElements / request.getSize());
      int startIndex = request.getPage() * request.getSize();
      int endIndex = Math.min(startIndex + request.getSize(), totalElements);

      List<TeacherStorageResponse> pagedTeachers = startIndex < totalElements ? teachers.subList(startIndex, endIndex)
          : List.of();

      return TeacherStorageListResponse.builder()
          .teachers(pagedTeachers)
          .totalPages(totalPages)
          .totalElements((long) totalElements)
          .currentPage(request.getPage())
          .pageSize(request.getSize())
          .build();
    }
  }

  public List<UserFileDetailResponse> getUserFileDetails(String userId) {
    List<StorageUsage> storageUsages = storageUsageRepository.findByUserId(userId);

    return storageUsages.stream()
        .map(usage -> new UserFileDetailResponse(
            usage.getId(),
            usage.getFileName(),
            usage.getFileUrl(),
            usage.getFileSize(),
            usage.getFileType(),
            usage.getUploadTime()))
        .collect(Collectors.toList());
  }

  public void deleteFileRecord(String fileUrl) {
    StorageUsage storageUsage = storageUsageRepository.findByFileUrl(fileUrl);
    if (storageUsage != null) {
      storageUsageRepository.delete(storageUsage);
    }
  }

  public boolean canUserUploadFile(String userId, Long fileSize) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      return false;
    }

    Long currentUsage = getTotalStorageUsedByUser(userId);
    Long storageLimit = user.getStorageLimit();

    return (currentUsage + fileSize) <= storageLimit;
  }

  public UserStorageInfoResponse getUserStorageInfo(String userId) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      throw new RuntimeException("User not found");
    }

    Long currentUsage = getTotalStorageUsedByUser(userId);
    Long storageLimit = user.getStorageLimit();
    Double usagePercentage = storageLimit > 0 ? (double) currentUsage / storageLimit * 100 : 0.0;
    Boolean isOverLimit = currentUsage > storageLimit;

    return UserStorageInfoResponse.builder()
        .userId(userId)
        .userName(user.getName())
        .userEmail(user.getEmail())
        .currentUsage(currentUsage)
        .storageLimit(storageLimit)
        .usagePercentage(usagePercentage)
        .formattedCurrentUsage(formatFileSize(currentUsage))
        .formattedStorageLimit(formatFileSize(storageLimit))
        .isOverLimit(isOverLimit)
        .build();
  }

  public UserStorageInfoResponse updateUserStorageLimit(String userId, Long newLimit) {
    User user = userRepository.findById(userId).orElse(null);
    if (user == null) {
      throw new RuntimeException("User not found");
    }

    if (newLimit < 0) {
      throw new RuntimeException("Storage limit cannot be negative");
    }

    user.setStorageLimit(newLimit);
    userRepository.save(user);

    return getUserStorageInfo(userId);
  }

  public List<UserStorageInfoResponse> getAllUsersStorageInfo() {
    List<User> users = userRepository.findAll();

    return users.stream()
        .map(user -> {
          Long currentUsage = getTotalStorageUsedByUser(user.getId());
          Long storageLimit = user.getStorageLimit();
          Double usagePercentage = storageLimit > 0 ? (double) currentUsage / storageLimit * 100 : 0.0;
          Boolean isOverLimit = currentUsage > storageLimit;

          return UserStorageInfoResponse.builder()
              .userId(user.getId())
              .userName(user.getName())
              .userEmail(user.getEmail())
              .currentUsage(currentUsage)
              .storageLimit(storageLimit)
              .usagePercentage(usagePercentage)
              .formattedCurrentUsage(formatFileSize(currentUsage))
              .formattedStorageLimit(formatFileSize(storageLimit))
              .isOverLimit(isOverLimit)
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * Format file size từ bytes sang human readable format
   */
  private String formatFileSize(Long bytes) {
    if (bytes == null || bytes == 0) {
      return "0 B";
    }

    String[] units = { "B", "KB", "MB", "GB", "TB" };
    int unitIndex = 0;
    double size = bytes;

    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }

    return String.format("%.2f %s", size, units[unitIndex]);
  }

  private List<TeacherStorageResponse> applyFuzzySearch(
      List<TeacherStorageResponse> teachers,
      TeacherSearchRequest request) {

    return teachers.stream()
        .map(teacher -> {
          double score = calculateSearchScore(teacher, request);
          teacher.setSearchScore(score);
          return teacher;
        })
        .filter(teacher -> teacher.getSearchScore() > 0.3)
        .sorted((t1, t2) -> Double.compare(t2.getSearchScore(), t1.getSearchScore()))
        .collect(Collectors.toList());
  }

  private double calculateSearchScore(TeacherStorageResponse teacher, TeacherSearchRequest request) {
    double totalScore = 0.0;
    int criteriaCount = 0;

    if (request.getName() != null && !request.getName().trim().isEmpty() && teacher.getName() != null) {
      double nameScore = calculateNameScore(teacher.getName(), request.getName());
      totalScore += nameScore;
      criteriaCount++;
    }

    if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && teacher.getEmail() != null) {
      int emailDistance = StringUtil.calculateLevenshteinDistance(
          teacher.getEmail().toLowerCase(),
          request.getEmail().toLowerCase());
      double emailScore = 1.0
          - (double) emailDistance / Math.max(teacher.getEmail().length(), request.getEmail().length());
      totalScore += emailScore;
      criteriaCount++;
    }

    return criteriaCount > 0 ? totalScore / criteriaCount : 1.0;
  }

  private double calculateNameScore(String fullName, String keyword) {
    String normalizedFullName = fullName.toLowerCase().trim();
    String normalizedKeyword = keyword.toLowerCase().trim();

    String[] nameWords = normalizedFullName.split("\\s+");
    String[] keywordWords = normalizedKeyword.split("\\s+");

    if (keywordWords.length == 0)
      return 1.0;

    double totalScore = 0.0;
    int matchedWords = 0;

    // Với mỗi từ trong keyword, tìm từ khớp nhất trong tên
    for (String keywordWord : keywordWords) {
      if (keywordWord.trim().isEmpty())
        continue;

      double bestMatchScore = 0.0;

      for (String nameWord : nameWords) {
        if (nameWord.trim().isEmpty())
          continue;

        double currentScore = calculateWordMatchScore(nameWord, keywordWord);
        bestMatchScore = Math.max(bestMatchScore, currentScore);
      }

      totalScore += bestMatchScore;
      matchedWords++;
    }

    if (matchedWords == 0)
      return 0.0;

    double averageScore = totalScore / matchedWords;

    double coverageBonus = 0.0;
    if (keywordWords.length > 1) {
      coverageBonus = (double) matchedWords / keywordWords.length * 0.05;
    }

    return Math.min(1.0, averageScore + coverageBonus);
  }

  private double calculateWordMatchScore(String nameWord, String keywordWord) {
    if (nameWord.equals(keywordWord)) {
      return 1.0;
    }

    if (nameWord.contains(keywordWord)) {
      double ratio = Math.min((double) keywordWord.length() / nameWord.length(), 1.0);
      return 0.7 + (ratio * 0.2);
    }

    if (keywordWord.contains(nameWord)) {
      double ratio = Math.min((double) nameWord.length() / keywordWord.length(), 1.0);
      return 0.6 + (ratio * 0.2);
    }

    int distance = StringUtil.calculateLevenshteinDistance(nameWord, keywordWord);
    int maxLength = Math.max(nameWord.length(), keywordWord.length());

    if (maxLength == 0)
      return 1.0;

    double similarity = 1.0 - (double) distance / maxLength;
    return similarity >= 0.4 ? similarity : 0.0;
  }
}