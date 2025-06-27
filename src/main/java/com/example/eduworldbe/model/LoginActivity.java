package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "login_activity", indexes = {
    @Index(name = "idx_login_activity_logintime", columnList = "loginTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginActivity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String userEmail;

  @Column(nullable = false)
  private Integer userRole; // 0: student, 1: teacher

  @Column(nullable = false)
  private String loginMethod; // "email", "google"

  @Column(nullable = false)
  private Date loginTime;

  private String ipAddress;
  private String userAgent;
}