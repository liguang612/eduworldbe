package com.example.eduworldbe.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user", indexes = {
    @Index(name = "idx_user_created_at", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String passwordHash;

  private String name;
  private Date birthday;
  private String school;
  private Integer grade;
  private String address;
  private Integer role; // 0: student, 1: teacher, 100: admin
  private String avatar;

  @Column(nullable = false)
  private Date createdAt;

  @Column(nullable = false)
  private Boolean isActive = true;

  @Column(nullable = false)
  private Long storageLimit = 78643200L; // 75MB
}