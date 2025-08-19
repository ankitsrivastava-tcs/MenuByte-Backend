package com.menubyte.dto;

import java.util.List;

public class BulkDeleteRequest {
    private List<Long> itemIds;
    private Long businessId; // Added for security validation

    // Getters and Setters
    public List<Long> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Long> itemIds) {
        this.itemIds = itemIds;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }
}