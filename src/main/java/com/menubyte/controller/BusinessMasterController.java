package com.menubyte.controller;

import com.menubyte.entity.BusinessMaster;
import com.menubyte.service.BusinessMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Business Master operations.
 * Provides endpoints for registering businesses, retrieving business data,
 * and updating subscription details.
 *
 * @author Ankit
 */
@RestController
@RequestMapping("/api/business-master")
public class BusinessMasterController {

    private final BusinessMasterService businessMasterService;

    /**
     * Constructor to initialize BusinessMasterService.
     *
     * @param businessMasterService the service layer for business master operations
     */
    public BusinessMasterController(BusinessMasterService businessMasterService) {
        this.businessMasterService = businessMasterService;
    }

    /**
     * Registers a new business for a user.
     *
     * @param businessMaster The business details to register
     * @return The registered business entity
     */
    @PostMapping("/register")
    public ResponseEntity<BusinessMaster> registerBusiness(@RequestBody BusinessMaster businessMaster) {
        BusinessMaster registeredBusiness = businessMasterService.registerBusiness(businessMaster);
        return ResponseEntity.ok(registeredBusiness);
    }

    /**
     * Retrieves all registered businesses (Admin view).
     *
     * @return List of all registered businesses
     */
    @GetMapping("/all")
    public ResponseEntity<List<BusinessMaster>> getAllRegisteredBusinesses() {
        List<BusinessMaster> businesses = businessMasterService.getAllRegisteredBusinesses();
        return ResponseEntity.ok(businesses);
    }

    /**
     * Retrieves all businesses registered by a specific user.
     *
     * @param userId The ID of the user whose businesses are to be fetched
     * @return List of businesses associated with the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BusinessMaster>> getBusinessesByUser(@PathVariable Long userId) {
        List<BusinessMaster> businesses = businessMasterService.getBusinessesByUser(userId);
        return ResponseEntity.ok(businesses);
    }

    /**
     * Updates subscription details for a business (e.g., renew or cancel subscription).
     *
     * @param id             The ID of the business to update
     * @param updatedDetails The updated subscription details
     * @return The updated business entity
     */
    @PutMapping("/{id}")
    public ResponseEntity<BusinessMaster> updateSubscription(@PathVariable Long id, @RequestBody BusinessMaster updatedDetails) {
        BusinessMaster updatedBusiness = businessMasterService.updateSubscription(id, updatedDetails);
        return ResponseEntity.ok(updatedBusiness);
    }
}