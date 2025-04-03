package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private com.menubyte.enums.BusinessType businessType;

    @JsonIgnore // Prevents recursion when serializing Business
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Menu menu;
}
