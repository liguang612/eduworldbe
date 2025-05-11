package com.example.eduworldbe.service;

import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public User register(User user) {
    user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
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

    // Cập nhật các trường thông tin
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
}
