package com.example.eduworldbe.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favourites", indexes = {
    @Index(name = "idx_favourite_type_target", columnList = "type, target_id"),
    @Index(name = "idx_favourite_user", columnList = "user_id"),
    @Index(name = "idx_favourite_type_user", columnList = "type, user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Favourite {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Integer type;

  @Column(name = "target_id", nullable = false)
  private String targetId;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}