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

import java.util.List;

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
}
