package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_description", "menu_id"})
})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_description", nullable = false)
    private String categoryDescription;

    @ManyToOne(optional = false)
    @JoinColumn(name = "master_category_id")
    @JsonIgnore
    private MasterCategory masterCategory;

    @ManyToOne(optional = false)
    @JoinColumn(name = "menu_id")
    @JsonIgnore
    private Menu menu;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items;

}
