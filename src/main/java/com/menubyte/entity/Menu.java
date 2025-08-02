package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;     // <--- NEW: Import for auditing
import org.springframework.data.annotation.LastModifiedDate;   // <--- NEW: Import for auditing
import org.springframework.data.jpa.domain.support.AuditingEntityListener; // <--- NEW: Import for auditing

import java.time.LocalDateTime; // <--- NEW: Import for auditing dates
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // <--- NEW: Enable JPA Auditing for this entity
public class Menu {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToOne
        @JoinColumn(name = "business_id", nullable = true)
        private Business business; // Each business has one menu

        @Column(nullable = false) // <--- Make sure menuName cannot be null
        @ColumnDefault("'Default Menu'") // <--- NEW: Provide a default value for existing rows
        private String menuName; // <--- UNCOMMENTED THIS LINE

        @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference("item-menu-ref")
        private List<Item> items;

        @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonManagedReference("menu-category-ref")
        private List<Category> categories;

        // --- NEW AUDITING FIELDS (for consistency with Item.java) ---
        @CreatedDate
        @Column(name = "created_date", nullable = true, updatable = false)
        private LocalDateTime createdDate;

        @LastModifiedDate
        @Column(name = "updated_date", nullable = true)
        private LocalDateTime updatedDate;
        // --- END NEW AUDITING FIELDS ---
}