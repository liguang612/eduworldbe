package com.example.eduworldbe.controller;

import com.example.eduworldbe.model.Favourite;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.service.FavouriteService;
import com.example.eduworldbe.util.AuthUtil;
import com.example.eduworldbe.dto.FavouriteDetailDTO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favourites")
@RequiredArgsConstructor
public class FavouriteController {
  @Autowired
  private AuthUtil authUtil;

  @Autowired
  private final FavouriteService favouriteService;

  @PostMapping("/{type}/{targetId}")
  public ResponseEntity<Favourite> addToFavourite(
      @PathVariable Integer type,
      @PathVariable String targetId,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    return ResponseEntity.ok(favouriteService.addToFavourite(type, targetId, currentUser.getId()));
  }

  @DeleteMapping("/{type}/{targetId}")
  public ResponseEntity<Void> removeFromFavourite(
      @PathVariable Integer type,
      @PathVariable String targetId,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    favouriteService.removeFromFavourite(type, targetId, currentUser.getId());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/{type}")
  public ResponseEntity<List<Favourite>> getFavouritesByType(
      @PathVariable Integer type,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    return ResponseEntity.ok(favouriteService.getFavouritesByType(type, currentUser.getId()));
  }

  @GetMapping("/{type}/subject/{subjectId}")
  public ResponseEntity<List<Favourite>> getFavouritesByTypeAndSubject(
      @PathVariable Integer type,
      @PathVariable String subjectId,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    return ResponseEntity.ok(favouriteService.getFavouritesByTypeAndSubject(type, currentUser.getId(), subjectId));
  }

  @GetMapping("/check/{type}/{targetId}")
  public ResponseEntity<Boolean> isFavourited(
      @PathVariable Integer type,
      @PathVariable String targetId,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    return ResponseEntity.ok(favouriteService.isFavourited(type, targetId, currentUser.getId()));
  }

  // New endpoints for detailed information
  @GetMapping("/detailed/{type}")
  public ResponseEntity<List<FavouriteDetailDTO>> getDetailedFavouritesByType(
      @PathVariable Integer type,
      @RequestParam(required = false) String keyword,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    return ResponseEntity.ok(favouriteService.getDetailedFavouritesByType(type, currentUser.getId(), keyword));
  }

  @GetMapping("/detailed/{type}/subject/{subjectId}")
  public ResponseEntity<List<FavouriteDetailDTO>> getDetailedFavouritesByTypeAndSubject(
      @PathVariable Integer type,
      @PathVariable String subjectId,
      @RequestParam(required = false) String keyword,
      HttpServletRequest request) {
    User currentUser = authUtil.getCurrentUser(request);
    if (currentUser == null) {
      throw new AccessDeniedException("User not authenticated");
    }

    return ResponseEntity
        .ok(favouriteService.getDetailedFavouritesByTypeAndSubject(type, currentUser.getId(), subjectId, keyword));
  }
}