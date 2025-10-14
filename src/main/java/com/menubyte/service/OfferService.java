package com.menubyte.service;

import com.menubyte.entity.Offer;
import com.menubyte.repository.OfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OfferService {

    @Autowired
    private OfferRepository offerRepository;

    // --- CREATE ---
    /**
     * Creates a new offer.
     */
    public Offer createOffer(Offer offer) {
        // Set default active status for creation
        offer.setActive(true);
        return offerRepository.save(offer);
    }

    // --- READ ---
    /**
     * Gets all offers in the database.
     */
    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    /**
     * Gets a single offer by its ID.
     */
    public Optional<Offer> getOfferById(Long id) {
        return offerRepository.findById(id);
    }

    // --- UPDATE ---
    /**
     * Updates an existing offer.
     */
    public Offer updateOffer(Long id, Offer offerDetails) {
        Optional<Offer> existingOfferOpt = offerRepository.findById(id);

        if (existingOfferOpt.isPresent()) {
            Offer existingOffer = existingOfferOpt.get();

            // Update fields from offerDetails (excluding ID and unique code usually)
            existingOffer.setTitle(offerDetails.getTitle());
            existingOffer.setOfferImageUrl(offerDetails.getOfferImageUrl());
            existingOffer.setTargetBusinessIds(offerDetails.getTargetBusinessIds());
            existingOffer.setTargetCategoryIds(offerDetails.getTargetCategoryIds());
            existingOffer.setDiscountValue(offerDetails.getDiscountValue());
            existingOffer.setDiscountType(offerDetails.getDiscountType());
            existingOffer.setMinimumCartValue(offerDetails.getMinimumCartValue());
            existingOffer.setStartDate(offerDetails.getStartDate());
            existingOffer.setEndDate(offerDetails.getEndDate());
            existingOffer.setVisibility(offerDetails.getVisibility());
            existingOffer.setActive(offerDetails.isActive()); // Allows toggling active state

            // IMPORTANT: The @PostLoad logic in the Entity handles the isOfferCurrentlyActive field.

            return offerRepository.save(existingOffer);
        } else {
            // Throw exception or return null based on preferred pattern
            throw new RuntimeException("Offer not found with id: " + id);
        }
    }

    // --- DELETE ---
    /**
     * Deletes an offer by its ID.
     */
    public void deleteOffer(Long id) {
        if (!offerRepository.existsById(id)) {
            throw new RuntimeException("Offer not found with id: " + id);
        }
        offerRepository.deleteById(id);
    }
    // Example signature for OfferService
    public List<Offer> getOffersByTargetBusinessId(Long businessId) {
        // Logic to call the repository with the correct query
        return offerRepository.findByTargetBusinessId(String.valueOf(businessId));
    }
}