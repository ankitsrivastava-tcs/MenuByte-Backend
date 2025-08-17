package com.menubyte.dto;


import java.util.List;

public class BulkItemCreationRequest {
    private Long businessId;
    private List<ItemCreationRequest> items;

    // Getters and Setters
    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public List<ItemCreationRequest> getItems() {
        return items;
    }

    public void setItems(List<ItemCreationRequest> items) {
        this.items = items;
    }
}