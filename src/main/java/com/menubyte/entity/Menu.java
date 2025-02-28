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
        @JsonManagedReference
        @JoinColumn(name = "business_id", nullable = false)
        private Business business; // Each business has one menu
        @JsonIgnore
        @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
        private List<Item> items; // A menu contains multiple items
}
