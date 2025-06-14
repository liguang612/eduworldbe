package com.example.eduworldbe.dto.request;

import lombok.Data;

@Data
public class AddLectureRequest {
  private String lectureId;

  public String getLectureId() {
    return lectureId;
  }

  public void setLectureId(String lectureId) {
    this.lectureId = lectureId;
  }
}