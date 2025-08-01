package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate; // Import
import org.springframework.data.annotation.LastModifiedDate; // Import
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // Import

import java.time.LocalDateTime; // Import if not already there

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // <--- Add this
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonBackReference("item-category-ref")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    @JsonBackReference("item-menu-ref")
    private Menu menu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_item_id")
    @JsonIgnore
    private MasterItem masterItem;

    // --- NEW AUDITING FIELDS ---
    // In your Item.java entity
    @CreatedDate
    @Column(name = "created_date", nullable = true, updatable = false) // Changed to true
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = true) // Changed to true
    private LocalDateTime updatedDate;
    // --- END NEW AUDITING FIELDS ---
}