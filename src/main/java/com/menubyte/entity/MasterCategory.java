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

    private String categoryDescription;

    @OneToMany(mappedBy = "masterCategory", cascade = CascadeType.ALL)
    @JsonManagedReference  // Allows serialization
    private List<Category> categories; // A master category has multiple sub-categories
}
