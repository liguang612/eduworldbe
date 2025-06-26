package com.example.eduworldbe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(ex.getReason());
  }

  @ExceptionHandler(StorageLimitExceededException.class)
  public ResponseEntity<Map<String, Object>> handleStorageLimitExceeded(StorageLimitExceededException ex) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", "STORAGE_LIMIT_EXCEEDED");
    response.put("message", ex.getMessage());
    response.put("status", "error");

    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
        .body(response);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
    // Log the exception for debugging
    ex.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body("An unexpected error occurred: " + ex.getMessage());
  }
}