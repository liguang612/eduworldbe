package com.example.eduworldbe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPageWithCursor {
  private List<NotificationResponse> notifications;
  private String nextCursor; // ID of the last item in the current list, to be used for 'startAfter'
  private boolean hasNextPage;
}