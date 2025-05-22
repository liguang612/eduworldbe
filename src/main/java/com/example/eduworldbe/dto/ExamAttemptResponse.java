package com.example.eduworldbe.dto;

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
}