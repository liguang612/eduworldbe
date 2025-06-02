package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.model.Favourite;
import com.example.eduworldbe.model.Lecture;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.FavouriteRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.dto.FavouriteDetailDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavouriteService {
  @Autowired
  private final FavouriteRepository favouriteRepository;

  @Autowired
  private final UserRepository userRepository;

  @Autowired
  private final CourseService courseService;

  @Autowired
  private final LectureService lectureService;

  @Autowired
  private final ExamService examService;

  @Transactional
  public Favourite addToFavourite(Integer type, String targetId, String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Favourite favourite = new Favourite();
    favourite.setType(type);
    favourite.setTargetId(targetId);
    favourite.setUser(user);

    return favouriteRepository.save(favourite);
  }

  @Transactional
  public void removeFromFavourite(Integer type, String targetId, String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    favouriteRepository.deleteByTypeAndTargetIdAndUser(type, targetId, user);
  }

  public List<Favourite> getFavouritesByType(Integer type, String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return favouriteRepository.findByTypeAndUser(type, user);
  }

  public List<Favourite> getFavouritesByTypeAndSubject(Integer type, String userId, String subjectId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return favouriteRepository.findByTypeAndUserAndSubjectId(type, user, subjectId);
  }

  public boolean isFavourited(Integer type, String targetId, String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return favouriteRepository.findByTypeAndTargetIdAndUser(type, targetId, user).isPresent();
  }

  // New method for efficient bulk checking
  public Set<String> getFavouritedTargetIds(Integer type, String userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return favouriteRepository.findTargetIdsByTypeAndUser(type, user);
  }

  // New methods to get detailed information
  public List<FavouriteDetailDTO> getDetailedFavouritesByType(Integer type, String userId) {
    List<Favourite> favourites = getFavouritesByType(type, userId);
    return favourites.stream()
        .map(this::convertToDetailDTO)
        .collect(Collectors.toList());
  }

  public List<FavouriteDetailDTO> getDetailedFavouritesByTypeAndSubject(Integer type, String userId,
      String subjectId) {
    List<Favourite> favourites = getFavouritesByTypeAndSubject(type, userId, subjectId);
    return favourites.stream()
        .map(this::convertToDetailDTO)
        .collect(Collectors.toList());
  }

  private FavouriteDetailDTO convertToDetailDTO(Favourite favourite) {
    FavouriteDetailDTO dto = new FavouriteDetailDTO();
    dto.setId(String.valueOf(favourite.getId()));
    dto.setUser(favourite.getUser());
    dto.setType(favourite.getType());
    dto.setTargetId(favourite.getTargetId());

    dto.setDetails(null);
    switch (favourite.getType()) {
      case 1:
        Course course = courseService.getById(favourite.getTargetId()).orElse(null);
        if (course != null) {
          dto.setDetails(courseService.toCourseResponse(course));
        }
        break;
      case 2:
        Lecture lecture = lectureService.getById(favourite.getTargetId()).orElse(null);
        if (lecture != null) {
          dto.setDetails(lectureService.toLectureResponse(lecture));
        }
        break;
      case 4:
        dto.setDetails(examService.getById(favourite.getTargetId()).orElse(null));
        break;
    }

    return dto;
  }
}