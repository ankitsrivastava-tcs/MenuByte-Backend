/**
 * Service for managing Business entities.
 * Handles business creation, retrieval, updates, and deletion.
 *
 * @author Ankit
 */
package com.menubyte.service;

import com.menubyte.dto.BusinessDTO;
import com.menubyte.entity.Business;
import com.menubyte.repository.BusinessRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    /**
     * Create a new Business.
     * @param business Business details.
     * @return Created Business object.
     */
    public Business createBusiness(Business business) {
        log.info("Creating new business: {}", business);
        Business createdBusiness = businessRepository.save(business);
        log.info("Business created successfully: {}", createdBusiness);
        return createdBusiness;
    }

    /**
     * Get a Business by ID.
     * @param id Business ID.
     * @return Business object.
     */
    public Business getBusinessById(Long id) {
        log.info("Fetching business with ID: {}", id);
        return businessRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Business not found with ID: {}", id);
                    return new RuntimeException("Business not found");
                });
    }

    /**
     * Get all Businesses for a specific user.
     * @param userId User ID.
     * @return List of BusinessDTO objects.
     */
    public List<BusinessDTO> getBusinessesByUserId(Long userId) {
        log.info("Fetching businesses for user ID: {}", userId);
        List<Business> businesses = businessRepository.findByUserId(userId);
        List<BusinessDTO> businessDTOs = businesses.stream()
                .map(b -> new BusinessDTO(b.getId(), b.getBusinessName(), b.getBusinessLogo(), b.getTagline(), b.getBusinessType()))
                .collect(Collectors.toList());
        log.info("Total businesses found for user {}: {}", userId, businessDTOs.size());
        return businessDTOs;
    }

    /**
     * Update Business details.
     * @param id Business ID.
     * @param updatedBusiness Updated Business details.
     * @return Updated Business object.
     */
    public Business updateBusiness(Long id, Business updatedBusiness) {
        log.info("Updating business with ID: {}", id);
        Business existingBusiness = getBusinessById(id);
        existingBusiness.setBusinessName(updatedBusiness.getBusinessName());
        existingBusiness.setBusinessLogo(updatedBusiness.getBusinessLogo());
        existingBusiness.setTagline(updatedBusiness.getTagline());
        Business updated = businessRepository.save(existingBusiness);
        log.info("Business updated successfully: {}", updated);
        return updated;
    }

    /**
     * Delete a Business.
     * @param id Business ID.
     */
    public void deleteBusiness(Long id) {
        log.info("Deleting business with ID: {}", id);
        Business business = getBusinessById(id);
        businessRepository.delete(business);
        log.info("Business deleted successfully with ID: {}", id);
    }
}