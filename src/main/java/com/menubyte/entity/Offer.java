package com.menubyte.entity;

import com.menubyte.enums.DiscountType;
import com.menubyte.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Ensure offerCode is unique across all offers
@Table(name = "offers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"offerCode"})
})
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The unique coupon code from the frontend (e.g., TASTY20)
    @Column(nullable = false, unique = true)
    private String offerCode;

    private String title;
    private String offerImageUrl;

    // Targetting Logic: 'ALL' or comma-separated list of Business IDs (e.g., "1,5,10")
    @Column(nullable = false)
    private String targetBusinessIds = "ALL";

    // Targetting Logic: 'ALL' or comma-separated list of Category IDs (e.g., "101,102")
    @Column(nullable = false)
    private String targetCategoryIds = "ALL";

    @Column(nullable = false)
    private Double discountValue;

    // Enum mapping the frontend value (PERCENTAGE or FIXED_AMOUNT)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // You'll need to define this Enum (see below)

    private Double minimumCartValue = 0.0;

    @Column(nullable = false)
    private LocalDate startDate; // Use LocalDate for date-only fields

    @Column(nullable = false)
    private LocalDate endDate;

    // Enum mapping the frontend value (ALL_USERS or ADMIN_ONLY)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility; // You'll need to define this Enum (see below)

    @Column(nullable = false)
    private boolean isActive = true; // Overall toggle

    // Custom Column: Calculates if the offer is active based on the date
    // @Transient or @Formula is better, but @PostLoad is easiest to ensure real-time status check.
    @Transient // This field is calculated and not stored in the database
    private Boolean isOfferCurrentlyActive;

    // Lifecycle hook to set the transient field after loading the entity
    @PostLoad
    @PostPersist
    @PostUpdate
    public void setIsOfferCurrentlyActive() {
        LocalDate today = LocalDate.now();
        // Check if today is between or equal to start and end date AND the offer is generally active
        this.isOfferCurrentlyActive = this.isActive &&
                !today.isBefore(this.startDate) &&
                !today.isAfter(this.endDate);
    }
}