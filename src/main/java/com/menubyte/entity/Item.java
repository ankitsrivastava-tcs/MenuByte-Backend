package com.menubyte.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
// import com.fasterxml.jackson.annotation.JsonIgnore; // <-- REMOVE THIS IMPORT if not used elsewhere
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private String itemDescription;
    private double itemDiscount;
    private String itemImage;

    @Enumerated(EnumType.STRING)
    private com.menubyte.enums.VegNonVeg vegOrNonVeg;

    private boolean itemAvailability;
    private boolean bestseller;
    @Column(name = "dealOfTheDay", nullable = true)
    private boolean dealOfTheDay;
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
    // @JsonIgnore // <-- REMOVE THIS LINE
    private MasterItem masterItem;

    @CreatedDate
    @Column(name = "created_date", nullable = true, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "updated_date", nullable = true)
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVariant> variants = new ArrayList<>();

}