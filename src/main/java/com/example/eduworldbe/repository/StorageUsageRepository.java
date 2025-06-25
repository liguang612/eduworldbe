package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.StorageUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StorageUsageRepository extends JpaRepository<StorageUsage, String> {
  @Query("SELECT SUM(su.fileSize) FROM StorageUsage su WHERE su.userId = :userId")
  Long getTotalStorageUsedByUser(@Param("userId") String userId);

  @Query("SELECT SUM(su.fileSize) FROM StorageUsage su")
  Long getTotalStorageUsed();

  @Query("SELECT su.userId, SUM(su.fileSize) as totalSize, COUNT(su.id) as fileCount " +
      "FROM StorageUsage su " +
      "GROUP BY su.userId ORDER BY totalSize DESC")
  List<Object[]> getStorageUsageByUser();

  @Query("SELECT su.fileType, SUM(su.fileSize) as totalSize, COUNT(su.id) as fileCount " +
      "FROM StorageUsage su " +
      "GROUP BY su.fileType ORDER BY totalSize DESC")
  List<Object[]> getStorageUsageByFileType();

  StorageUsage findByFileUrl(String fileUrl);
}