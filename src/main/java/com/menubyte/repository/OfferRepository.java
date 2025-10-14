package com.menubyte.repository;

import com.menubyte.entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    // Custom method to fetch offers for a specific business ID (if needed later)
    // Note: Since targetBusinessIds is a String, exact matching is difficult.
    // You might use a native query or simple finding for specific business logic.
    // Example: List<Offer> findByTargetBusinessIdsContaining(String businessId);
    @Query("SELECT o FROM Offer o WHERE o.targetBusinessIds = 'ALL' OR o.targetBusinessIds LIKE %:businessIdString% ")
    List<Offer> findByTargetBusinessId(@Param("businessIdString") String businessIdString);

}