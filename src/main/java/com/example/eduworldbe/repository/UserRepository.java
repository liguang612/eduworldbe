package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.Date;

public interface UserRepository extends JpaRepository<User, String> {
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Long countByRole(Integer role);

  @Query("SELECT YEAR(u.createdAt) as year, MONTH(u.createdAt) as month, u.role as role, COUNT(u.id) as count " +
      "FROM User u " +
      "WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate " +
      "GROUP BY YEAR(u.createdAt), MONTH(u.createdAt), u.role " +
      "ORDER BY year, month, role")
  List<Object[]> getMonthlyUserCountsByRole(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

  // Query cho tìm kiếm với nhiều tiêu chí
  @Query("SELECT u FROM User u WHERE " +
  // "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
  // "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND
  // " +
      "(:role IS NULL OR u.role = :role) AND " +
      "(:isActive IS NULL OR u.isActive = :isActive)")
  Page<User> findUsersWithFilters(
      @Param("name") String name,
      @Param("email") String email,
      @Param("role") Integer role,
      @Param("isActive") Boolean isActive,
      Pageable pageable);
}
