package com.menubyte.dto;

public class TopSellingItemDTO {
    private String itemName;
    private long totalSold;

    public TopSellingItemDTO(String itemName, Long totalSold) {
        this.itemName = itemName;
        this.totalSold = totalSold;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public long getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(long totalSold) {
        this.totalSold = totalSold;
    }
}