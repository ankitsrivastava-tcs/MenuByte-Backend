package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String itemDescription;
    private double itemPrice;
    private double itemDiscount;
    private String itemImage;

    @Enumerated(EnumType.STRING)
    private com.menubyte.entity.VegNonVeg vegOrNonVeg; // Assuming enum is in enums package

    private boolean itemAvailability;
    private boolean bestseller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference("item-category-ref") // Unique name for this back-reference
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    @JsonBackReference("item-menu-ref") // Unique name for this back-reference
    private Menu menu;

    // NEW: Optional link to MasterItem to avoid duplication and allow customization
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_item_id") // This column is nullable
    @JsonIgnore // Ignore to prevent recursion if MasterItem has a back-reference
    private MasterItem masterItem;
}
