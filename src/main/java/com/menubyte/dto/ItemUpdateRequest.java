package com.menubyte.dto;
// src/main/java/com/menubyte/dto/ItemUpdateRequest.java

import com.menubyte.entity.Item; // Import if you need VegOrNonVeg enum
import com.menubyte.enums.VegNonVeg;

import javax.swing.text.html.parser.Entity;

public class ItemUpdateRequest {
    private String itemName;
    private String itemDescription;
    private Double price; // Ensure this matches your Item entity's price type
    private Double itemDiscount;
    private String itemImage;
    private VegNonVeg vegOrNonVeg; // Use the enum from Item.java if defined there
    private Boolean itemAvailability;
    private Boolean bestseller;
    private Long categoryId; // This is the key change: accept categoryId directly
    // private Long masterItemId; // Add if you need to update masterItem as well

    // Constructor (optional, but good for building in tests)
    public ItemUpdateRequest() {}

    // Getters and Setters for all fields
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getItemDiscount() {
        return itemDiscount;
    }

    public void setItemDiscount(Double itemDiscount) {
        this.itemDiscount = itemDiscount;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public VegNonVeg getVegOrNonVeg() {
        return vegOrNonVeg;
    }

    public void setVegOrNonVeg(VegNonVeg vegOrNonVeg) {
        this.vegOrNonVeg = vegOrNonVeg;
    }

    public Boolean getItemAvailability() {
        return itemAvailability;
    }

    public void setItemAvailability(Boolean itemAvailability) {
        this.itemAvailability = itemAvailability;
    }

    public Boolean getBestseller() {
        return bestseller;
    }

    public void setBestseller(Boolean bestseller) {
        this.bestseller = bestseller;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    // public Long getMasterItemId() { return masterItemId; }
    // public void setMasterItemId(Long masterItemId) { this.masterItemId = masterItemId; }

    @Override
    public String toString() {
        return "ItemUpdateRequest{" +
                "itemName='" + itemName + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", price=" + price +
                ", itemDiscount=" + itemDiscount +
                ", vegOrNonVeg=" + vegOrNonVeg +
                ", itemAvailability=" + itemAvailability +
                ", bestseller=" + bestseller +
                ", categoryId=" + categoryId +
                '}';
    }
}
