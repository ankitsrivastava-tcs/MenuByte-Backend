package com.menubyte.entity;

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
    private com.menubyte.entity.VegNonVeg vegOrNonVeg; // VEG or NON-VEG

    private boolean itemAvailability;
    private boolean bestseller;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // Links to Category

    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu; // Links to the Menu
}
