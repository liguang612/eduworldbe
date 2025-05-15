package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.SharedMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharedMediaRepository extends JpaRepository<SharedMedia, String> {
  List<SharedMedia> findByMediaType(Integer mediaType);

  List<SharedMedia> findByTitleContaining(String title);
}