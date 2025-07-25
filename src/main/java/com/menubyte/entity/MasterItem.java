package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasterItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) // Ensure itemName is not null
    private String itemName;

    private String itemDescription;
    private double itemPrice;
    private String itemImage;

    @ManyToOne(fetch = FetchType.LAZY) // Added LAZY fetch
    @JoinColumn(name = "master_category_id", nullable = true) // <--- CHANGED: Set nullable to true
    @JsonBackReference("master-category-item-ref") // Unique name for this back-reference
    private MasterCategory masterCategory; // Corrected field name to match entity type
}
