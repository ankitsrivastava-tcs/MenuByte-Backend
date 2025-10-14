package com.menubyte.controller;

import com.menubyte.entity.Offer;
import com.menubyte.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/offers")
public class OfferController {

    @Autowired
    private OfferService offerService;

    private ResponseEntity<?> validateOffer(Offer offer) {
        if (offer.getStartDate() == null || offer.getEndDate() == null) {
            return ResponseEntity.badRequest().body("Start date and End date are required.");
        }
        if (offer.getStartDate().isAfter(offer.getEndDate())) {
            return ResponseEntity.badRequest().body("Start date cannot be after end date.");
        }
        return null; // Validation passed
    }

    // =======================================================
    // 1. CREATE (POST) - (Unchanged)
    // =======================================================
    @PostMapping
    public ResponseEntity<?> createOffer(@RequestBody Offer offer) {
        ResponseEntity<?> validationError = validateOffer(offer);
        if (validationError != null) return validationError;

        try {
            offer.setId(null);
            Offer createdOffer = offerService.createOffer(offer);
            return new ResponseEntity<>(createdOffer, HttpStatus.CREATED);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("ConstraintViolationException")) {
                return new ResponseEntity<>("Failed to create offer: Offer Code must be unique.", HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>("Failed to create offer: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // =======================================================
    // 2. READ ALL / READ BY BUSINESS ID (GET) - CHANGED
    // =======================================================
    /**
     * GET /api/offers/all
     * GET /api/offers/all?businessId={id}
     * Fetches all offers OR offers targeted to a specific business ID.
     */
    @GetMapping("/all")
    public ResponseEntity<List<Offer>> getAllOffers(
            @RequestParam(required = false) Long businessId // <-- NEW PARAMETER
    ) {
        List<Offer> offers;

        if (businessId != null) {
            // Call service method to filter offers by the target business ID
            // NOTE: You MUST implement this new service method in OfferService
            offers = offerService.getOffersByTargetBusinessId(businessId);
        } else {
            // Default behavior: Fetch all offers
            offers = offerService.getAllOffers();
        }

        return ResponseEntity.ok(offers);
    }

    // =======================================================
    // 3. READ SINGLE (GET) - (Unchanged)
    // =======================================================
    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        Optional<Offer> offer = offerService.getOfferById(id);
        return offer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =======================================================
    // 4. UPDATE (PUT) - (Unchanged)
    // =======================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateOffer(@PathVariable Long id, @RequestBody Offer offerDetails) {
        ResponseEntity<?> validationError = validateOffer(offerDetails);
        if (validationError != null) return validationError;

        try {
            Offer updatedOffer = offerService.updateOffer(id, offerDetails);
            return ResponseEntity.ok(updatedOffer);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // =======================================================
    // 5. DELETE (DELETE) - (Unchanged)
    // =======================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        try {
            offerService.deleteOffer(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}