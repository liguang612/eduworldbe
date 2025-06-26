package com.example.eduworldbe.service;

import com.example.eduworldbe.dto.request.UserSearchRequest;
import com.example.eduworldbe.dto.response.UserListResponse;
import com.example.eduworldbe.dto.response.UserResponse;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public UserListResponse getAllUsers(UserSearchRequest request) {
    Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

    Page<User> userPage = userRepository.findUsersWithFilters(
        request.getName(),
        request.getEmail(),
        request.getRole(),
        request.getIsActive(),
        pageable);

    List<UserResponse> userResponses = userPage.getContent().stream()
        .map(this::convertToUserResponse)
        .collect(Collectors.toList());

    if (request.hasSearchCriteria()) {
      userResponses = applyFuzzySearch(userResponses, request);

      return UserListResponse.builder()
          .users(userResponses)
          .totalPages(1)
          .totalElements((long) userResponses.size())
          .currentPage(0)
          .pageSize(userResponses.size())
          .build();
    } else {
      // Khi không tìm kiếm, sử dụng phân trang từ database
      return UserListResponse.builder()
          .users(userResponses)
          .totalPages(userPage.getTotalPages())
          .totalElements(userPage.getTotalElements())
          .currentPage(request.getPage())
          .pageSize(request.getSize())
          .build();
    }
  }

  public User changeUserRole(String userId, Integer newRole) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (newRole < 0 || newRole > 2) {
      throw new RuntimeException("Invalid role. Role must be 0 (student), 1 (teacher), or 2 (admin)");
    }

    user.setRole(newRole);
    return userRepository.save(user);
  }

  public User toggleUserStatus(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    user.setIsActive(!user.getIsActive());
    return userRepository.save(user);
  }

  public static String generatePassword(int length) {
    final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";

    SecureRandom random = new SecureRandom();
    StringBuilder password = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(CHARACTERS.length());
      password.append(CHARACTERS.charAt(index));
    }

    return password.toString();
  }

  public String resetUserPassword(String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    String newPassword = generatePassword(10);
    user.setPasswordHash(passwordEncoder.encode(newPassword));

    userRepository.save(user);

    return newPassword;
  }

  private UserResponse convertToUserResponse(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getAvatar(),
        user.getSchool(),
        user.getGrade(),
        user.getAddress(),
        user.getRole(),
        user.getBirthday() != null ? user.getBirthday().toString() : null,
        user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
        user.getIsActive(),
        null,
        user.getStorageLimit()
    );
  }

  private List<UserResponse> applyFuzzySearch(
      List<UserResponse> users,
      UserSearchRequest request) {

    return users.stream()
        .map(user -> {
          double score = calculateSearchScore(user, request);
          user.setSearchScore(score);
          return user;
        })
        .filter(user -> user.getSearchScore() > 0.3)
        .sorted((u1, u2) -> Double.compare(u2.getSearchScore(), u1.getSearchScore()))
        .collect(Collectors.toList());
  }

  private double calculateSearchScore(UserResponse user, UserSearchRequest request) {
    double totalScore = 0.0;
    int criteriaCount = 0;

    if (request.getName() != null && !request.getName().trim().isEmpty() && user.getName() != null) {
      double nameScore = calculateNameScore(user.getName(), request.getName());
      totalScore += nameScore;
      criteriaCount++;
    }

    if (request.getEmail() != null && !request.getEmail().trim().isEmpty() && user.getEmail() != null) {
      int emailDistance = StringUtil.calculateLevenshteinDistance(
          user.getEmail().toLowerCase(),
          request.getEmail().toLowerCase());
      double emailScore = 1.0
          - (double) emailDistance / Math.max(user.getEmail().length(), request.getEmail().length());
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
    return similarity >= 0.6 ? similarity : 0.0;
  }
}