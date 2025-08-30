package com.menubyte.repository;

import com.menubyte.dto.DailySalesDTO;
import com.menubyte.dto.TopSellingItemDTO;
import com.menubyte.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
    List<Order> findByBusinessIdAndCreatedAtAfterOrderByCreatedAtDesc(Long businessId, LocalDateTime date);

    @Query("SELECT MIN(o.createdAt) FROM Order o WHERE o.businessId = :businessId")
    Optional<LocalDateTime> findEarliestOrderDate(@Param("businessId") Long businessId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.businessId = :businessId AND o.createdAt BETWEEN :startDate AND :endDate")
    double findTotalSalesByBusinessAndPeriod(@Param("businessId") Long businessId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.businessId = :businessId AND o.createdAt BETWEEN :startDate AND :endDate")
    int findTotalOrdersByBusinessAndPeriod(@Param("businessId") Long businessId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // In OrderRepository.java
    @Query(value = "SELECT CAST(created_at AS DATE), SUM(total_amount) " +
            "FROM orders " +
            "WHERE business_id = :businessId AND created_at BETWEEN :startDate AND :endDate " +
            "GROUP BY CAST(created_at AS DATE) ORDER BY 1 ASC", nativeQuery = true)
    List<Object[]> findDailySalesByBusinessAndPeriod(@Param("businessId") Long businessId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    @Query("SELECT new com.menubyte.dto.TopSellingItemDTO(oi.itemName, SUM(oi.quantity)) " +
            "FROM Order o JOIN o.orderItems oi " +
            "WHERE o.businessId = :businessId AND o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY oi.itemName ORDER BY SUM(oi.quantity) DESC")
    List<TopSellingItemDTO> findTopSellingItemsByBusinessAndPeriod(@Param("businessId") Long businessId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}