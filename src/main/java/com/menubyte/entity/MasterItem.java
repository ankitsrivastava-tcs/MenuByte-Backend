package com.menubyte.entity;

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

    private String itemDescription;
    private double itemPrice;
    private String itemImage;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private MasterCategory category; // Links to Master Category
}
