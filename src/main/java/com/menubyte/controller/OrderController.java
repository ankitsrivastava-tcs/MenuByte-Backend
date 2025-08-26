package com.menubyte.controller;

import com.menubyte.entity.Order;
import com.menubyte.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
@Autowired
OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<Order>> getOrdersByBusinessId(@PathVariable Long businessId) {
        List<Order> orders = orderService.getOrdersByBusinessId(businessId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/business/{businessId}/today")
    public ResponseEntity<List<Order>> getTodaysOrdersByBusinessId(@PathVariable Long businessId) {
        List<Order> orders = orderService.getTodaysOrdersByBusinessId(businessId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/business/{businessId}/today-sale")
    public ResponseEntity<Double> getTodaysSale(@PathVariable Long businessId) {
        double totalSale = orderService.calculateTodaysSale(businessId);
        return ResponseEntity.ok(totalSale);
    }
}