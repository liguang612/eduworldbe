package com.example.eduworldbe.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
  @MessageMapping("/subscribe")
  @SendToUser("/queue/notifications")
  public String subscribeToNotifications(@Payload String message, Principal principal) {
    log.info("User {} subscribed to notifications", principal.getName());
    return "Subscribed to notifications";
  }
}