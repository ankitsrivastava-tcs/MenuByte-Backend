package com.menubyte.dto;

import java.time.LocalDate;

public class DailySalesDTO {
    private LocalDate date;
    private double sales;

    public DailySalesDTO(LocalDate date, double sales) {
        this.date = date;
        this.sales = sales;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public double getSales() {
        return sales;
    }

    public void setSales(double sales) {
        this.sales = sales;
    }
}
