/**
 * Service for managing BusinessMaster entities.
 * Handles business registration, retrieval, and subscription updates.
 *
 * @author Ankit
 */
package com.menubyte.service;

import com.menubyte.entity.BusinessMaster;
import com.menubyte.repository.BusinessMasterRepository;
import com.menubyte.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BusinessMasterService {

    private final BusinessMasterRepository businessMasterRepository;
    private final UserRepository userRepository;

    public BusinessMasterService(BusinessMasterRepository businessMasterRepository, UserRepository userRepository) {
        this.businessMasterRepository = businessMasterRepository;
        this.userRepository = userRepository;
    }

    /**
     * Register a business for a user.
     * @param businessMaster Business details.
     * @return Registered BusinessMaster object.
     */
    public BusinessMaster registerBusiness(BusinessMaster businessMaster) {
        log.info("Registering business: {}", businessMaster);
        return businessMasterRepository.save(businessMaster);
    }

    /**
     * Get all registered businesses (Admin View).
     * @return List of all registered businesses.
     */
    public List<BusinessMaster> getAllRegisteredBusinesses() {
        log.info("Fetching all registered businesses");
        return businessMasterRepository.findAll();
    }

    /**
     * Get all businesses registered by a specific user.
     * @param userId User ID.
     * @return List of businesses owned by the user.
     */
    public List<BusinessMaster> getBusinessesByUser(Long userId) {
        log.info("Fetching businesses for user ID: {}", userId);
        return businessMasterRepository.findByUserId(userId);
    }
    public BusinessMaster getBusinessesByBusinessID(Long businessID) {
        log.info("Fetching businesses for user ID: {}", businessID);
        return businessMasterRepository.findByBusinessId(businessID);
    }

    /**
     * Update subscription details.
     * @param id Business ID.
     * @param updatedDetails Updated subscription details.
     * @return Updated BusinessMaster object.
     */
    public BusinessMaster updateSubscription(Long id, BusinessMaster updatedDetails) {
        log.info("Updating subscription for business ID: {}", id);
        BusinessMaster existingBusiness = businessMasterRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Business Master entry not found"));

        existingBusiness.setEndDate(updatedDetails.getEndDate());
        existingBusiness.setAmountPaid(updatedDetails.getAmountPaid());
        existingBusiness.setSubscriptionStatus(updatedDetails.getSubscriptionStatus());

        BusinessMaster updatedBusiness = businessMasterRepository.save(existingBusiness);
        log.info("Updated subscription details: {}", updatedBusiness);
        return updatedBusiness;
    }
    /**
     * Delete a business by ID.
     * @param businessId Business ID to delete.
     * @throws RuntimeException if business not found.
     */
    @Transactional
    public void deleteBusiness(Long businessId) {
        log.info("Attempting to delete business with ID: {}", businessId);

        // Check if business exists
        BusinessMaster businessOptional = businessMasterRepository.findByBusinessId(businessId);
        if (null == businessOptional) {
            log.error("Business with ID {} not found for deletion", businessId);
            throw new RuntimeException("Business not found with ID: " + businessId);
        } else {
            try {
                businessMasterRepository.deleteById(businessOptional.getId());
            } catch (Exception e) {
                log.error("Error deleting business with ID {}: {}", businessId, e.getMessage());
                throw new RuntimeException("Failed to delete business: " + e.getMessage());
            }
        }}

}


