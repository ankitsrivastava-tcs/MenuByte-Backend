package com.menubyte.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessMaster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate registerDate;
    private LocalDate endDate;
    private double amountPaid;

    @Enumerated(EnumType.STRING)
    private com.menubyte.entity.SubscriptionStatus subscriptionStatus; // ACTIVE / INACTIVE

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Tracks which user registered

    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business; // Tracks the registered business
}
