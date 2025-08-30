package com.menubyte.dto;

import java.util.List;

public class AnalyticsResponseDTO {
    private double totalSales;
    private int totalOrders;
    private double averageOrderValue;
    private List<DailySalesDTO> dailySales;
    private List<TopSellingItemDTO> topSellingItems;

    // Constructors, getters, and setters
    public AnalyticsResponseDTO(double totalSales, int totalOrders, double averageOrderValue, List<DailySalesDTO> dailySales, List<TopSellingItemDTO> topSellingItems) {
        this.totalSales = totalSales;
        this.totalOrders = totalOrders;
        this.averageOrderValue = averageOrderValue;
        this.dailySales = dailySales;
        this.topSellingItems = topSellingItems;
    }

    // Getters and Setters
    public double getTotalSales() {
        return totalSales;
    }
    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }
    public int getTotalOrders() {
        return totalOrders;
    }
    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }
    public double getAverageOrderValue() {
        return averageOrderValue;
    }
    public void setAverageOrderValue(double averageOrderValue) {
        this.averageOrderValue = averageOrderValue;
    }
    public List<DailySalesDTO> getDailySales() {
        return dailySales;
    }
    public void setDailySales(List<DailySalesDTO> dailySales) {
        this.dailySales = dailySales;
    }
    public List<TopSellingItemDTO> getTopSellingItems() {
        return topSellingItems;
    }
    public void setTopSellingItems(List<TopSellingItemDTO> topSellingItems) {
        this.topSellingItems = topSellingItems;
    }
}