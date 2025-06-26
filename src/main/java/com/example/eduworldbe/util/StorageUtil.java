package com.example.eduworldbe.util;

public class StorageUtil {
  public static String formatFileSize(Long bytes) {
    if (bytes == null || bytes == 0) {
      return "0 B";
    }

    String[] units = { "B", "KB", "MB", "GB", "TB" };
    int unitIndex = 0;
    double size = bytes;

    while (size >= 1024 && unitIndex < units.length - 1) {
      size /= 1024;
      unitIndex++;
    }

    return String.format("%.2f %s", size, units[unitIndex]);
  }
}
