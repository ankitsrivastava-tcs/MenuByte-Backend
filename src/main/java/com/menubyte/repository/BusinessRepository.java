package com.menubyte.repository;

import com.menubyte.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {

    /**
     * Finds a Business by its ID and the ID of the User who owns it.
     */
    Optional<Business> findByIdAndUserId(Long businessId, Long userId);

    /**
     * Finds all Businesses owned by a specific User ID.
     */
    List<Business> findByUserId(Long userId);

    /**
     * Finds a Business by its name (case-insensitive).
     */
    Optional<Business> findByBusinessNameIgnoreCase(String businessName);

    /**
     * Finds all Businesses by a specific BusinessType.
     */
    List<Business> findByBusinessType(com.menubyte.enums.BusinessType businessType);
}
