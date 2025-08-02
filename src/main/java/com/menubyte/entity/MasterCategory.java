package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasterCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_description", unique = true, nullable = false)
    private String categoryDescription;

    // Named JsonManagedReference for the 'categories' list
    @OneToMany(mappedBy = "masterCategory", fetch = FetchType.LAZY)
    @JsonManagedReference("master-category-category-ref") // Unique name for this managed-reference
    private List<Category> categories; // A master category has multiple sub-categories

    // NEW: Optional - if you want to link MasterItems directly to MasterCategory
    @OneToMany(mappedBy = "masterCategory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("master-category-item-ref") // Unique name for this managed-reference
    private List<MasterItem> masterItems;
    @Enumerated(EnumType.STRING)
    private com.menubyte.enums.BusinessType businessType;

}
