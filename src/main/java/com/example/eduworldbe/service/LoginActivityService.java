package com.example.eduworldbe.service;

import com.example.eduworldbe.model.LoginActivity;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.LoginActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class LoginActivityService {

  @Autowired
  private LoginActivityRepository loginActivityRepository;

  public void recordLoginActivity(User user, String loginMethod, HttpServletRequest request) {
    LoginActivity activity = new LoginActivity();
    activity.setUserId(user.getId());
    activity.setUserEmail(user.getEmail());
    activity.setUserRole(user.getRole());
    activity.setLoginMethod(loginMethod);
    activity.setLoginTime(new Date());

    // Lấy IP address
    String ipAddress = getClientIpAddress(request);
    activity.setIpAddress(ipAddress);

    // Lấy User Agent
    String userAgent = request.getHeader("User-Agent");
    activity.setUserAgent(userAgent);

    loginActivityRepository.save(activity);
  }

  private String getClientIpAddress(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
      return xForwardedFor.split(",")[0];
    }

    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
      return xRealIp;
    }

    return request.getRemoteAddr();
  }
}