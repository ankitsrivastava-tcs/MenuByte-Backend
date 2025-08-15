package com.menubyte.dto;

import com.menubyte.entity.Item;
import com.menubyte.enums.VegNonVeg;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private Long id;

    private String itemName;
    // REMOVE THIS: private double price;
    private double itemDiscount;
    private String itemImage;

    private VegNonVeg vegOrNonVeg;

    private boolean bestseller;
    private boolean itemAvailability;
    private String itemDescription;
    private boolean dealOfTheDay;

    private Long masterItemId;

    // ADD THIS: New field for multiple price variants
    private List<ItemVariantDto> variants;

    public ItemDTO(Item item) {
        this.id = item.getId();
        this.itemName = item.getItemName();
        // REMOVE THIS: this.price = item.getItemPrice();
        this.itemDiscount = item.getItemDiscount();
        this.itemImage = item.getItemImage();
        this.vegOrNonVeg = item.getVegOrNonVeg();
        this.bestseller = item.isBestseller();
        this.itemAvailability = item.isItemAvailability();
        this.itemDescription = item.getItemDescription();
        this.dealOfTheDay = item.isDealOfTheDay();
        this.masterItemId = item.getMasterItem() != null ? item.getMasterItem().getId() : null;

        // ADD THIS: Populate the variants list from the entity
        if (item.getVariants() != null) {
            this.variants = item.getVariants().stream()
                    .map(variant -> new ItemVariantDto(variant.getVariantName(), variant.getPrice()))
                    .collect(Collectors.toList());
        }
    }
}