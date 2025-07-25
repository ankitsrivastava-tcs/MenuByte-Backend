package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne
        @JoinColumn(name = "business_id", nullable = false)
        private Business business; // Each business has one menu

        // Consider adding a menu name if needed
        // private String menuName;

        // Named JsonManagedReference for the 'items' list
        @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference("item-menu-ref") // <--- This was already named
        private List<Item> items;

        // IMPORTANT: Ensure this 'categories' list is present and has a unique named JsonManagedReference
        // This matches the @JsonBackReference("menu-category-ref") in Category.java
        @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference("menu-category-ref") // <--- Added/Ensured this unique name
        private List<Category> categories;
}
