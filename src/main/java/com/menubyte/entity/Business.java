package com.menubyte.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

    // Changed from @JsonIgnore to @JsonBackReference with a unique name
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-business-ref") // Unique name for this back-reference
    private User user;

    @OneToOne(mappedBy = "business", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Menu is the owning side, so ignore here to prevent recursion
    private Menu menu;
}
