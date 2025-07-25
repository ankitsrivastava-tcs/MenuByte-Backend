package com.menubyte.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessMaster { // Consider renaming to BusinessSubscription or BusinessRegistration
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate registerDate;
    private LocalDate endDate;
    private double amountPaid;

    @Enumerated(EnumType.STRING)
    private com.menubyte.entity.SubscriptionStatus subscriptionStatus; // Assuming enum is in enums package

    // No JsonManagedReference/JsonBackReference needed here as it's a one-way relationship for JSON serialization
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Tracks which user registered

    // No JsonManagedReference/JsonBackReference needed here as it's a one-way relationship for JSON serialization
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business; // Tracks the registered business
}
