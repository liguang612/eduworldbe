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

    // Áp dụng fuzzy search nếu có tìm kiếm
    if (request.getName() != null || request.getEmail() != null) {
      userResponses = applyFuzzySearch(userResponses, request);
    }

    return UserListResponse.builder()
        .users(userResponses)
        .totalPages(userPage.getTotalPages())
        .totalElements(userPage.getTotalElements())
        .currentPage(request.getPage())
        .pageSize(request.getSize())
        .build();
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
        null // searchScore sẽ được set sau
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
        .filter(user -> user.getSearchScore() > 0.3) // Chỉ giữ lại kết quả có điểm > 0.3
        .sorted((u1, u2) -> Double.compare(u2.getSearchScore(), u1.getSearchScore()))
        .collect(Collectors.toList());
  }

  private double calculateSearchScore(UserResponse user, UserSearchRequest request) {
    double totalScore = 0.0;
    int criteriaCount = 0;

    if (request.getName() != null && !request.getName().trim().isEmpty()) {
      int nameDistance = StringUtil.calculateLevenshteinDistance(
          user.getName().toLowerCase(),
          request.getName().toLowerCase());
      double nameScore = 1.0 - (double) nameDistance / Math.max(user.getName().length(), request.getName().length());
      totalScore += nameScore;
      criteriaCount++;
    }

    if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
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
}