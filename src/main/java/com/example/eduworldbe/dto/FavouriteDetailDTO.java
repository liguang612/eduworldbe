package com.example.eduworldbe.dto;

import com.example.eduworldbe.model.User;

import lombok.Data;

@Data
public class FavouriteDetailDTO {
  private String id;
  private User user;
  private Integer type;
  private String targetId;

  // Detailed object information
  private Object details;
}