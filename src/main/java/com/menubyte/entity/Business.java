package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.menubyte.enums.BusinessType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String businessName;
    private String businessLogo;
    private String tagline;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY) // 🔴 Add LAZY loading
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @OneToOne(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY) // 🔴 Add LAZY loading
    @JsonIgnore
    @JsonBackReference
    private Menu menu;
}
