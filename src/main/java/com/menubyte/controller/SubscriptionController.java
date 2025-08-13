package com.menubyte.controller;

import com.menubyte.entity.BusinessMaster;
import com.menubyte.repository.BusinessMasterRepository;
import com.menubyte.service.BusinessMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for retrieving subscription records.
 * Provides a dedicated endpoint for the front-end Subscription component.
 *
 * @author Ankit
 */
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {


    private final BusinessMasterService businessMasterService;

    /**
     * Constructor to initialize BusinessMasterService.
     *
     * @param businessMasterService the service layer for business master operations
     */
    public SubscriptionController(BusinessMasterService businessMasterService) {
        this.businessMasterService = businessMasterService;
    }

    /**
     * Retrieves all subscription records for a specific user.
     * The front-end Subscription component calls this endpoint.
     *
     * @param userId The ID of the user whose subscription records are to be fetched
     * @return A list of subscription records associated with the user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BusinessMaster>> getSubscriptionsByUser(@PathVariable Long userId) {
        List<BusinessMaster> subscriptions = businessMasterService.getBusinessesByUser(userId);
        return ResponseEntity.ok(subscriptions);
    }
}