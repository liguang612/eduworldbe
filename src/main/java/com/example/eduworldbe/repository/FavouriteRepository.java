package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.Favourite;
import com.example.eduworldbe.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
  Optional<Favourite> findByTypeAndTargetIdAndUser(Integer type, String targetId, User user);

  List<Favourite> findByTypeAndUser(Integer type, User user);

  @Query("SELECT f FROM Favourite f WHERE f.type = :type AND f.user = :user AND " +
      "((:type = 1 AND EXISTS (SELECT c FROM Course c WHERE c.id = f.targetId)) OR " +
      "(:type = 2 AND EXISTS (SELECT l FROM Lecture l WHERE l.id = f.targetId)) OR " +
      "(:type = 4 AND EXISTS (SELECT e FROM Exam e WHERE e.id = f.targetId)))")
  List<Favourite> findByTypeAndUserAndSubjectId(
      @Param("type") Integer type,
      @Param("user") User user,
      @Param("subjectId") String subjectId);

  void deleteByTypeAndTargetIdAndUser(Integer type, String targetId, User user);

  @Query("SELECT f.targetId FROM Favourite f WHERE f.type = :type AND f.user = :user")
  Set<String> findTargetIdsByTypeAndUser(@Param("type") Integer type, @Param("user") User user);
}