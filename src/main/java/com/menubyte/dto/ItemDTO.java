package com.menubyte.dto;

import com.menubyte.entity.Item;
import lombok.Data;

@Data
public class ItemDTO {
    private String itemName;
    private double price;

    public ItemDTO(Item item) {
        this.itemName = item.getItemName();
        this.price = item.getItemPrice();
    }
}
