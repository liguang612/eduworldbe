package com.example.eduworldbe.service;

import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.example.eduworldbe.dto.UserInfoDTO;
import com.example.eduworldbe.dto.response.UserSearchResponse;
import com.google.firebase.auth.FirebaseToken;
import java.util.Date;

@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public User register(User user) {
    user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
    user.setCreatedAt(new Date());
    user.setIsActive(true);
    return userRepository.save(user);
  }

  public User findByEmail(String email) {
    return userRepository.findByEmail(email).orElse(null);
  }

  public User findById(String id) {
    return userRepository.findById(id).orElse(null);
  }

  public User update(String id, User updatedUser) {
    User existingUser = findById(id);
    if (existingUser == null) {
      throw new RuntimeException("User not found");
    }

    if (updatedUser.getName() != null) {
      existingUser.setName(updatedUser.getName());
    }
    if (updatedUser.getAvatar() != null) {
      existingUser.setAvatar(updatedUser.getAvatar());
    }
    if (updatedUser.getSchool() != null) {
      existingUser.setSchool(updatedUser.getSchool());
    }
    if (updatedUser.getGrade() != null) {
      existingUser.setGrade(updatedUser.getGrade());
    }
    if (updatedUser.getAddress() != null) {
      existingUser.setAddress(updatedUser.getAddress());
    }
    if (updatedUser.getBirthday() != null) {
      existingUser.setBirthday(updatedUser.getBirthday());
    }

    return userRepository.save(existingUser);
  }

  public void changePassword(String id, String currentPassword, String newPassword) {
    User user = findById(id);
    if (user == null) {
      throw new RuntimeException("User not found");
    }

    // Kiểm tra mật khẩu hiện tại
    if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
      throw new RuntimeException("Current password is incorrect");
    }

    // Cập nhật mật khẩu mới
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    userRepository.save(user);
  }

  public List<UserSearchResponse> searchUsers(String emailQuery, Integer role) {
    List<User> allUsers = userRepository.findAll();

    return allUsers.stream()
        .filter(user -> user.getRole().equals(role))
        .filter(user -> calculateLevenshteinDistance(user.getEmail().toLowerCase(), emailQuery.toLowerCase()) <= 3)
        .map(user -> new UserSearchResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getAvatar(),
            user.getSchool(),
            user.getGrade()))
        .collect(Collectors.toList());
  }

  private int calculateLevenshteinDistance(String s1, String s2) {
    int[][] dp = new int[s1.length() + 1][s2.length() + 1];

    for (int i = 0; i <= s1.length(); i++) {
      dp[i][0] = i;
    }
    for (int j = 0; j <= s2.length(); j++) {
      dp[0][j] = j;
    }

    for (int i = 1; i <= s1.length(); i++) {
      for (int j = 1; j <= s2.length(); j++) {
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1];
        } else {
          dp[i][j] = 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
        }
      }
    }

    return dp[s1.length()][s2.length()];
  }

  public UserInfoDTO getUserInfo(String userId) {
    User user = findById(userId);
    if (user == null) {
      throw new RuntimeException("User not found");
    }

    UserInfoDTO userInfo = new UserInfoDTO();
    userInfo.setId(userId);
    userInfo.setUserName(user.getName());
    userInfo.setUserAvatar(user.getAvatar());
    userInfo.setUserSchool(user.getSchool());

    return userInfo;
  }

  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  public User createUserFromGoogle(FirebaseToken decodedToken) {
    User user = new User();
    user.setEmail(decodedToken.getEmail());
    user.setName(decodedToken.getName());
    user.setAvatar(decodedToken.getPicture());
    user.setCreatedAt(new Date());
    user.setIsActive(true);
    return user;
  }
}
