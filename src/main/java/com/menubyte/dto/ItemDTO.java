package com.menubyte.dto;

import com.menubyte.entity.Item;
import com.menubyte.enums.VegNonVeg;
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
    private String itemImage; // <-- Added this, your entity has it but DTO didn't

    private VegNonVeg vegOrNonVeg;

    private boolean bestseller;
    private boolean itemAvailability; // <-- Added this, your entity has it but DTO didn't
    private String itemDescription;
    private boolean dealOfTheDay;

    private Long masterItemId; // <-- NEW FIELD

    public ItemDTO(Item item) {
        this.id = item.getId();
        this.itemName = item.getItemName();
        this.price = item.getItemPrice();
        this.itemDiscount = item.getItemDiscount();
        this.itemImage = item.getItemImage(); // <-- Populate here
        this.vegOrNonVeg = item.getVegOrNonVeg();
        this.bestseller = item.isBestseller();
        this.itemAvailability = item.isItemAvailability(); // <-- Populate here
        this.itemDescription = item.getItemDescription();
        this.dealOfTheDay=item.isDealOfTheDay();
        this.masterItemId = item.getMasterItem() != null ? item.getMasterItem().getId() : null; // <-- Populate masterItemId
    }
}