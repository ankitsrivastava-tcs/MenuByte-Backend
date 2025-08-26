package com.menubyte.service;

import com.menubyte.entity.Order;
import com.menubyte.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getOrdersByBusinessId(Long businessId) {
        return orderRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
    }

    public List<Order> getTodaysOrdersByBusinessId(Long businessId) {
        LocalDate today = LocalDate.now();
        return orderRepository.findByBusinessIdAndCreatedAtAfterOrderByCreatedAtDesc(businessId, today.atStartOfDay());
    }

    public double calculateTodaysSale(Long businessId) {
        return getTodaysOrdersByBusinessId(businessId).stream()
                .mapToDouble(order -> order.getTotalAmount().doubleValue())
                .sum();
    }
}