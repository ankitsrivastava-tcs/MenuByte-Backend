package com.menubyte.repository;

import com.menubyte.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBusinessIdOrderByCreatedAtDesc(Long businessId);
    List<Order> findByBusinessIdAndCreatedAtAfterOrderByCreatedAtDesc(Long businessId, LocalDateTime date);
}
