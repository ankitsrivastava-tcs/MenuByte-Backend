package com.menubyte.dto;

public class PaymentRequest {
    private int amount;
    private String tableNumber; // Add this

    // Getters and Setters
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
}