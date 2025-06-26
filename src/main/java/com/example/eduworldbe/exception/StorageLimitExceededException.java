package com.example.eduworldbe.exception;

public class StorageLimitExceededException extends RuntimeException {
  public StorageLimitExceededException(String message) {
    super(message);
  }

  public StorageLimitExceededException(String message, Throwable cause) {
    super(message, cause);
  }
}