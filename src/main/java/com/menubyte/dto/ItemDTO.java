package com.menubyte.dto;

import com.menubyte.entity.Item;
import com.menubyte.entity.VegNonVeg;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private Long id;

    private String itemName;
    private double price;
    private double itemDiscount;

    private VegNonVeg vegOrNonVeg; // VEG or NON-VEG

    private boolean bestseller;
    private String itemDescription;

    public ItemDTO(Item item) {
        this.id=item.getId();
        this.itemName = item.getItemName();
        this.price = item.getItemPrice();
        this.itemDiscount = item.getItemDiscount();
        this.vegOrNonVeg = item.getVegOrNonVeg();
        this.bestseller = item.isBestseller();
        this.itemDescription = item.getItemDescription();
    }
}
