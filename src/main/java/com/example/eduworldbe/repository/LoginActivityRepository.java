package com.example.eduworldbe.repository;

import com.example.eduworldbe.model.LoginActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface LoginActivityRepository extends JpaRepository<LoginActivity, String> {

  @Query("SELECT COUNT(DISTINCT la.userId) FROM LoginActivity la WHERE DATE(la.loginTime) = DATE(:date)")
  Long countUniqueUsersByDate(@Param("date") Date date);

  @Query("SELECT COUNT(DISTINCT la.userId) FROM LoginActivity la WHERE la.userRole = :role AND DATE(la.loginTime) = DATE(:date)")
  Long countUniqueUsersByRoleAndDate(@Param("role") Integer role, @Param("date") Date date);

  @Query("SELECT DATE(la.loginTime) as date, COUNT(DISTINCT la.userId) as count FROM LoginActivity la " +
      "WHERE la.loginTime >= :startDate AND la.loginTime <= :endDate " +
      "GROUP BY DATE(la.loginTime) ORDER BY date")
  List<Object[]> getDailyUserCounts(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query("SELECT DATE(la.loginTime) as date, la.userRole as role, COUNT(DISTINCT la.userId) as count FROM LoginActivity la "
      +
      "WHERE la.loginTime >= :startDate AND la.loginTime <= :endDate " +
      "GROUP BY DATE(la.loginTime), la.userRole ORDER BY date, role")
  List<Object[]> getDailyUserCountsByRole(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

  @Query("SELECT YEAR(la.loginTime) as year, MONTH(la.loginTime) as month, COUNT(DISTINCT la.userId) as count FROM LoginActivity la "
      +
      "WHERE la.loginTime >= :startDate AND la.loginTime <= :endDate " +
      "GROUP BY YEAR(la.loginTime), MONTH(la.loginTime) ORDER BY year, month")
  List<Object[]> getMonthlyUserCounts(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

  Long countByLoginTimeBetween(Date startDate, Date endDate);
}