package com.menubyte.service;

import com.menubyte.dto.MenuDTO;
import com.menubyte.entity.Business;
import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import com.menubyte.entity.User;
import com.menubyte.repository.BusinessRepository;
import com.menubyte.repository.MenuRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        return menuRepository.findByBusinessId(businessId)
                .orElseThrow(() -> {
                    log.error("No menu found for business ID: {}", businessId);
                    return new RuntimeException("No menu found for this business.");
                });
    }

    /**
     * Find Menu by Business ID.
     * @param businessId Business ID.
     * @return Optional of Menu.
     */
    public Optional<Menu> findByBusinessId(Long businessId) {
        log.info("Finding menu for business ID: {}", businessId);
        return menuRepository.findByBusinessId(businessId);
    }

    /**
     * Updates the items of a menu for a given business and user.
     */
    public Menu updateMenuItems(Long businessId, User user, MenuDTO updatedMenuDTO) {
        log.info("Updating menu items for business ID: {}", businessId);

        Menu menu = getMenuForUserBusiness(businessId, user);

        // Remove existing items and add new ones
        menu.getItems().clear();
        for (Item item : updatedMenuDTO.getItems()) {
            item.setMenu(menu);  // Ensure items belong to the same menu
            menu.getItems().add(item);
        }

        // Save updated menu
        menu = menuRepository.save(menu);
        log.info("Menu items updated successfully for business ID: {}", businessId);

        return menu;
    }
}
