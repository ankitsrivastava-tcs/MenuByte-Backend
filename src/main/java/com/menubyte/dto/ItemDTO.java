package com.menubyte.dto;

import com.menubyte.entity.Item;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class ItemDTO {
    private String itemName;
    private double price;
    private double itemDiscount;
    @Enumerated(EnumType.STRING)
    private com.menubyte.entity.VegNonVeg vegOrNonVeg; // VEG or NON-VEG
    private boolean bestseller;
    private String itemDescription;

    public ItemDTO(Item item) {
        this.itemName = item.getItemName();
        this.price = item.getItemPrice();
        this.itemDiscount=item.getItemDiscount();
        this.vegOrNonVeg=item.getVegOrNonVeg();
        this.bestseller=item.isBestseller();
        this.itemDescription=item.getItemDescription();
    }
}
