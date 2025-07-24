package com.menubyte.entity;

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
    private com.menubyte.entity.VegNonVeg vegOrNonVeg;

    private boolean itemAvailability;
    private boolean bestseller;

    @ManyToOne(fetch = FetchType.EAGER) // <--- Changed to EAGER fetch type
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore // Prevents serialization of the category to avoid circular reference
    private Category category;

    @ManyToOne
    @JoinColumn(name = "menu_id", nullable = false)
    @JsonIgnore
    private Menu menu;
}
