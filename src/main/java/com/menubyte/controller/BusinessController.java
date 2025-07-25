package com.menubyte.controller;

import com.menubyte.dto.BusinessDTO;
import com.menubyte.entity.Business;
import com.menubyte.service.BusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing business-related operations.
 * This controller provides endpoints for creating, retrieving,
 * updating, and deleting businesses, as well as fetching businesses
 * associated with a specific user.
 *
 * @author Ankit
 * @version 1.0
 */
@RestController
@RequestMapping("/api/business")
@Slf4j // Enable logging for this controller

public class BusinessController {

    private final BusinessService businessService;

    /**
     * Constructor for BusinessController.
     *
     * @param businessService The service handling business logic.
     */
    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    /**
     * Creates a new business.
     *
     * @param business The business entity to be created.
     * @return ResponseEntity containing the created business.
     */
    /**
     * Creates a new business.
     * The userId is now passed as a path parameter.
     *
     * @param userId The ID of the user creating the business.
     * @return ResponseEntity containing the created business.
     */
    @PostMapping("/create/user/{userId}") // Changed mapping to include userId as path variable
    public ResponseEntity<Business> createBusiness(
            @PathVariable Long userId, // userId now comes from path
            @RequestBody Business business) { // Request body is the DTO

        // Pass the userId and DTO to the service
        Business createdBusiness = businessService.createBusiness(userId, business);
        return ResponseEntity.ok(createdBusiness);
    }

    /**
     * Retrieves a business by its ID.
     *
     * @param id The ID of the business.
     * @return ResponseEntity containing the business.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Business> getBusinessById(@PathVariable Long id) {
        Business business = businessService.getBusinessById(id);
        return ResponseEntity.ok(business);
    }

    /**
     * Retrieves all businesses associated with a specific user.
     *
     * @param userId The ID of the user.
     * @return ResponseEntity containing a list of BusinessDTO objects.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBusinessesByUser(@PathVariable Long userId) {
        try {
                List<BusinessDTO> businesses = businessService.getBusinessesByUserId(userId);
            return ResponseEntity.ok(businesses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching businesses: " + e.getMessage());
        }
    }

    /**
     * Updates business details.
     *
     * @param id The ID of the business to be updated.
     * @return ResponseEntity containing the updated business.
     */
    @PutMapping("/{id}/user/{userId}") // Updated mapping to include userId as path variable
    public ResponseEntity<Business> updateBusiness(
            @PathVariable Long id,
            @PathVariable Long userId, // userId now comes from path
            @RequestBody Business businessReq) {

        // Pass the userId to the service for authorization check
        Business business = businessService.updateBusiness(id, businessReq);
        return ResponseEntity.ok(business);
    }


    /**
     * Deletes a business by its ID.
     *
     * @param id The ID of the business to be deleted.
     * @return ResponseEntity confirming deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusiness(id);
        return ResponseEntity.ok("Business deleted successfully.");
    }
}
