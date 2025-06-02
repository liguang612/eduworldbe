package com.example.eduworldbe.service;

import com.example.eduworldbe.model.Course;
import com.example.eduworldbe.model.Exam;
import com.example.eduworldbe.model.Favourite;
import com.example.eduworldbe.model.Lecture;
import com.example.eduworldbe.model.User;
import com.example.eduworldbe.repository.FavouriteRepository;
import com.example.eduworldbe.repository.UserRepository;
import com.example.eduworldbe.dto.ExamResponse;
import com.example.eduworldbe.dto.FavouriteDetailDTO;
import com.example.eduworldbe.dto.CourseResponse;
import com.example.eduworldbe.dto.LectureResponse;
import com.example.eduworldbe.util.StringUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.AbstractMap;

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
  public List<FavouriteDetailDTO> getDetailedFavouritesByType(Integer type, String userId, String keyword) {
    List<Favourite> favourites = getFavouritesByType(type, userId);
    List<FavouriteDetailDTO> detailedFavourites = favourites.stream()
        .map(this::convertToDetailDTO)
        .collect(Collectors.toList());

    if (keyword != null && !keyword.trim().isEmpty()) {
      detailedFavourites = filterFavouritesByKeyword(detailedFavourites, keyword);
    }
    return detailedFavourites;
  }

  public List<FavouriteDetailDTO> getDetailedFavouritesByTypeAndSubject(Integer type, String userId, String subjectId,
      String keyword) {
    List<Favourite> favourites = getFavouritesByTypeAndSubject(type, userId, subjectId);
    List<FavouriteDetailDTO> detailedFavourites = favourites.stream()
        .map(this::convertToDetailDTO)
        .collect(Collectors.toList());

    if (keyword != null && !keyword.trim().isEmpty()) {
      detailedFavourites = filterFavouritesByKeyword(detailedFavourites, keyword);
    }
    return detailedFavourites;
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
        Exam exam = examService.getById(favourite.getTargetId()).orElse(null);
        if (exam != null) {
          ExamResponse examResponse = examService.toExamResponse(exam);
          examResponse.setFavourite(true);

          dto.setDetails(examResponse);
        }
        break;
    }

    return dto;
  }

  private List<FavouriteDetailDTO> filterFavouritesByKeyword(List<FavouriteDetailDTO> dtos, String keyword) {
    if (keyword == null || keyword.trim().isEmpty()) {
      return dtos;
    }
    String[] searchTerms = keyword.toLowerCase().split("\\s+");

    return dtos.stream()
        .map(dto -> {
          String itemName = "";
          Object details = dto.getDetails();

          if (details instanceof CourseResponse) {
            itemName = ((CourseResponse) details).getName();
          } else if (details instanceof LectureResponse) {
            itemName = ((LectureResponse) details).getName();
          } else if (details instanceof ExamResponse) {
            itemName = ((ExamResponse) details).getTitle();
          }

          if (itemName == null || itemName.trim().isEmpty()) {
            return new AbstractMap.SimpleEntry<>(dto, 0.0);
          }

          itemName = itemName.toLowerCase();
          double score = 0.0;

          for (String term : searchTerms) {
            double termScore = 0.0;
            if (itemName.equals(term)) {
              termScore += 100.0;
            } else if (itemName.contains(term)) {
              double lengthRatio = (double) term.length() / itemName.length();
              termScore += 80.0 * lengthRatio;
            }

            int distance = StringUtil.calculateLevenshteinDistance(itemName, term);
            if (distance <= 3) {
              termScore += Math.max(0, 30.0 * (1 - (double) distance / 3.0));
            }
            String[] itemWords = itemName.split("\\s+");
            for (String word : itemWords) {
              distance = StringUtil.calculateLevenshteinDistance(word, term);
              if (distance <= 2) {
                termScore += Math.max(0, 20.0 * (1 - (double) distance / 2.0));
              }
            }
            score += termScore;
          }
          score = (searchTerms.length > 0) ? (score / searchTerms.length) : 0.0;
          return new AbstractMap.SimpleEntry<>(dto, score);
        })
        .filter(entry -> entry.getValue() > 0)
        .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
        .map(AbstractMap.SimpleEntry::getKey)
        .collect(Collectors.toList());
  }
}