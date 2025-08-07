/**
 * Service for managing Business entities.
 * Handles business creation, retrieval, updates, and deletion.
 *
 * @author Ankit
 */
package com.menubyte.service;

import com.menubyte.dto.BusinessDTO;
import com.menubyte.entity.Business;
import com.menubyte.entity.BusinessMaster;
import com.menubyte.entity.Menu;
import com.menubyte.entity.User;
import com.menubyte.enums.SubscriptionStatus;
import com.menubyte.enums.SubscriptionType;
import com.menubyte.exception.BusinessCountException;
import com.menubyte.exception.UserAlreadyExistsException;
import com.menubyte.repository.BusinessMasterRepository;
import com.menubyte.repository.BusinessRepository;
import com.menubyte.repository.MenuRepository;
import com.menubyte.repository.UserRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BusinessService {

    private final BusinessRepository businessRepository;
    private final UserRepository  userRepository ;
    private final MenuRepository menuRepository ;
    private final BusinessMasterRepository businessMasterRepository ;
    private final BusinessMasterService businessMasterService ;



    public BusinessService(BusinessRepository businessRepository, UserRepository userRepository, MenuRepository menuRepository,BusinessMasterRepository businessMasterRepository,BusinessMasterService businessMasterService) {
        this.businessRepository = businessRepository;
        this.userRepository = userRepository;
        this.menuRepository = menuRepository;
        this.businessMasterRepository=businessMasterRepository;
        this.businessMasterService=businessMasterService;
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
        businessMasterService.deleteBusiness(id);

        businessRepository.delete(business);
        log.info("Business deleted successfully with ID: {}", id);
    }
    // In BusinessService.java
    public Business createBusiness(long userId,Business business) {
        // Assuming business.getUser() contains at least the ID from the frontend
        // In a real app, you'd get the user from the authenticated context
        checkBusinessCountAsPerSubscriptionType(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        business.setUser(user);
        // You might also want to create a default menu for the business here
        Business savedBusiness = businessRepository.save(business);
        Menu newMenu = new Menu();
        newMenu.setMenuName("Default");
        newMenu.setBusiness(savedBusiness);
        menuRepository.save(newMenu); // Save the new menu
        savedBusiness.setMenu(newMenu); // Link the menu back to the business entity

        BusinessMaster businessMaster= new BusinessMaster();
        businessMaster.setBusiness(savedBusiness);
        businessMaster.setUser(user);
        businessMaster.setAmountPaid(0);
        businessMaster.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        businessMaster.setSubscriptionType(SubscriptionType.TRIAL);
        businessMaster.setRegisterDate(LocalDate.now());
        businessMaster.setEndDate(LocalDate.now().plusWeeks(1));
        businessMasterRepository.save(businessMaster);
        return savedBusiness;
    }

    private void checkBusinessCountAsPerSubscriptionType(long userId) {
        List<BusinessMaster> businessMasterList=businessMasterRepository.findByUserId(userId);
        if(businessMasterList.size()>0) {
            if (businessMasterList.size() == 1 && businessMasterList.get(0).getSubscriptionType().name().equalsIgnoreCase(String.valueOf(SubscriptionType.TRIAL))) {
                throw new BusinessCountException("Can not add more than 1 business in Trial version.Buy subscription to proceed.");
            }

        }
    }
}