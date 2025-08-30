package com.menubyte.service;

import com.menubyte.dto.AnalyticsResponseDTO;
import com.menubyte.dto.DailySalesDTO;
import com.menubyte.dto.TopSellingItemDTO;
import com.menubyte.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;

    @Autowired
    public AnalyticsService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public AnalyticsResponseDTO getSalesAnalytics(Long businessId, String period) {
        LocalDateTime startDateTime;
        LocalDate endDate = LocalDate.now();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        switch (period) {
            case "30d":
                startDateTime = endDate.minusDays(30).atStartOfDay();
                break;
            case "all":
                startDateTime = orderRepository.findEarliestOrderDate(businessId)
                        .orElse(LocalDateTime.of(2000, 1, 1, 0, 0, 0));
                break;
            case "7d":
            default:
                startDateTime = endDate.minusDays(7).atStartOfDay();
                break;
        }

        double totalSales = orderRepository.findTotalSalesByBusinessAndPeriod(businessId, startDateTime, endDateTime);
        int totalOrders = orderRepository.findTotalOrdersByBusinessAndPeriod(businessId, startDateTime, endDateTime);
        double averageOrderValue = totalOrders > 0 ? totalSales / totalOrders : 0;

        // Fetch results as List<Object[]> from the native query
        List<Object[]> dailySalesData = orderRepository.findDailySalesByBusinessAndPeriod(businessId, startDateTime, endDateTime);

        // Manually map the Object[] array to your DailySalesDTO
        List<DailySalesDTO> dailySales = dailySalesData.stream()
                .map(result -> {
                    LocalDate date = null;
                    if (result[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) result[0]).toLocalDate();
                    } else if (result[0] instanceof LocalDate) {
                        date = (LocalDate) result[0];
                    }
                    return new DailySalesDTO(
                            date,
                            ((java.math.BigDecimal) result[1]).doubleValue() // Correctly convert BigDecimal to double
                    );
                })
                .collect(Collectors.toList());

        List<TopSellingItemDTO> topSellingItems = orderRepository.findTopSellingItemsByBusinessAndPeriod(businessId, startDateTime, endDateTime);

        return new AnalyticsResponseDTO(totalSales, totalOrders, averageOrderValue, dailySales, topSellingItems);
    }
}