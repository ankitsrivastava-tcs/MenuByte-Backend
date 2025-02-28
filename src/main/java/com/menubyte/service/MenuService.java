/**
 * Service for managing Menu entities.
 * Handles menu retrieval for a user's business.
 *
 * @author Ankit Srivastava
 */
package com.menubyte.service;

import com.menubyte.entity.Business;
import com.menubyte.entity.Menu;
import com.menubyte.entity.User;
import com.menubyte.repository.BusinessRepository;
import com.menubyte.repository.MenuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final BusinessRepository businessRepository;

    public MenuService(MenuRepository menuRepository, BusinessRepository businessRepository) {
        this.menuRepository = menuRepository;
        this.businessRepository = businessRepository;
    }

    /**
     * Get Menu for a User's Business.
     * @param businessId Business ID.
     * @param user User requesting the menu.
     * @return Menu object.
     */
    public Menu getMenuForUserBusiness(Long businessId, User user) {
        log.info("Fetching menu for business ID: {} and user ID: {}", businessId, user.getId());
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> {
                    log.error("Business not found with ID: {}", businessId);
                    return new RuntimeException("Business not found");
                });

        if (!business.getUser().getId().equals(user.getId())) {
            log.error("Unauthorized access attempt by user ID: {} for business ID: {}", user.getId(), businessId);
            throw new RuntimeException("Unauthorized! You don't own this business.");
        }

        Menu menu = menuRepository.findByBusinessId(businessId)
                .orElseThrow(() -> {
                    log.error("No menu found for business ID: {}", businessId);
                    return new RuntimeException("No menu found for this business.");
                });

        log.info("Menu retrieved successfully for business ID: {}", businessId);
        return menu;
    }
}