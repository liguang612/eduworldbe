package com.example.eduworldbe.dto.response;

import java.util.Date;
import java.util.Map;

import com.example.eduworldbe.model.ExamAttempt;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class ExamAttemptResponse extends ExamAttempt {
  private Map<String, String> savedAnswers;
  private Date updatedAt;
  private Boolean shuffleChoice;
  private Boolean shuffleQuestion;
}