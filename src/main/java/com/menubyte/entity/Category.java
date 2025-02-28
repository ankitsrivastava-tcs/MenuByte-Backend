package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String categoryDescription;

    @ManyToOne
    @JoinColumn(name = "master_category_id", nullable = false)
    @JsonBackReference
    private MasterCategory masterCategory; // Links to MasterCategory
}
