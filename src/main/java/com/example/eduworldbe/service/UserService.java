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
}
