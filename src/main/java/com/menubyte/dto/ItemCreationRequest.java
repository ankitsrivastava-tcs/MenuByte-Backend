package com.menubyte.dto;

// src/main/java/com/menubyte/dto/ItemCreationRequest.java
import com.menubyte.enums.VegNonVeg;
import lombok.Data; // Assuming Lombok is used

@Data // Generates getters, setters, toString, equals, hashCode
public class ItemCreationRequest {
    private String itemName;
    private String itemDescription;
    private Double itemPrice; // Use Double for nullable numbers from frontend
    private Double itemDiscount; // Use Double for nullable numbers from frontend
    private String itemImage;
    private VegNonVeg vegOrNonVeg; // Ensure this matches your enum type
    private Boolean itemAvailability;
    private Boolean bestseller;

    // --- Category Information (mutually exclusive based on isNewCategory) ---
    private Boolean isNewCategory; // Flag to indicate if it's a new category
    private Boolean   isNewItem;
    private String categoryDescription; // Used if isNewCategory is true (the name of the new category)
    private Long categoryId; // Used if isNewCategory is false (the ID of the existing category)

    // --- Related Entities IDs ---
    private Long menuId; // The ID of the menu this item belongs to
    private Long masterItemId; // Optional: The ID of the master item if selected
    private Long userId; // The ID of the user (if you still need it in the body)
}
