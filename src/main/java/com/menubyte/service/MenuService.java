package com.menubyte.service;

import com.fasterxml.jackson.databind.ObjectMapper; // Not used in this method, can be removed if not used elsewhere
import com.menubyte.dto.CategoryDTO;
import com.menubyte.dto.ItemDTO;
import com.menubyte.dto.MenuDTO;
import com.menubyte.entity.*;
import com.menubyte.mapper.ItemMapper; // Ensure ItemMapper is correctly imported
import com.menubyte.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils; // Not used in updateMenuItems anymore, can be removed if not used elsewhere
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate; // Import Hibernate utility for initialization

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository; // Not used in updateMenuItems, can be removed if not used elsewhere
    private final MasterCategoryRepository masterCategoryRepository; // Not used in updateMenuItems, can be removed if not used elsewhere
    private final ItemRepository itemRepository;

    public MenuService(MenuRepository menuRepository,
                       BusinessRepository businessRepository,
                       CategoryRepository categoryRepository,
                       MasterCategoryRepository masterCategoryRepository, ItemRepository itemRepository) {
        this.menuRepository = menuRepository;
        this.businessRepository = businessRepository;
        this.categoryRepository = categoryRepository;
        this.masterCategoryRepository = masterCategoryRepository;
        this.itemRepository = itemRepository;
    }

    /**
     * Get Menu for a User's Business.
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
     */
    public Optional<Menu> findByBusinessId(Long businessId) {
        log.info("Finding menu for business ID: {}", businessId);
        return menuRepository.findByBusinessId(businessId);
    }

    /**
     * Updates the items of a menu for a given business and user.
     */
    @Transactional
    public Menu updateMenuItems(Long businessId, User user, MenuDTO updatedMenuDTO) {
        log.info("Updating menu items for business ID: {}", businessId);

        // 1. Fetch the existing Menu entity from the database
        Menu existingMenu = getMenuForUserBusiness(businessId, user);

        // Explicitly initialize the 'items' collection within the transaction
        Hibernate.initialize(existingMenu.getItems());

        // 2. Iterate through the categories and items provided in the DTO
        for (CategoryDTO categoryDto : updatedMenuDTO.getCategories()) {
            if (categoryDto.getId() == null) {
                log.warn("Skipping category update: CategoryDTO for '{}' has no ID. Cannot find existing category.", categoryDto.getCategoryName());
                continue;
            }

            for (ItemDTO itemDto : categoryDto.getItems()) {
                if (itemDto.getId() == null) {
                    log.warn("Skipping item update: ItemDTO for '{}' has no ID. Cannot find existing item.", itemDto.getItemName());
                    continue;
                }

                // Find the existing Item entity in the database using category ID, menu ID, and item ID
                Optional<Item> existingItemOptional = itemRepository.findByCategoryIdAndMenuIdAndId(
                        categoryDto.getId(),
                        existingMenu.getId(),
                        itemDto.getId()
                );

                if (existingItemOptional.isPresent()) {
                    Item existingItem = existingItemOptional.get();

                    // Explicitly initialize the 'category' association of the found item
                    Hibernate.initialize(existingItem.getCategory());

                    // Copy properties from the DTO to the existing entity using the mapper
                    ItemMapper.updateEntityFromDto(itemDto, existingItem);

                    // Save the updated existing item.
                    itemRepository.save(existingItem);
                    log.info("Successfully updated item: ID={}, Name={}", existingItem.getId(), existingItem.getItemName());
                } else {
                    log.warn("Item with ID {} (Name: {}) not found in category {} or menu {} for update. Skipping.",
                            itemDto.getId(), itemDto.getItemName(), categoryDto.getId(), existingMenu.getId());
                }
            }
        }

        // After all updates, ensure the menu's items and their categories are fully loaded
        // before returning, as the DTO conversion happens outside this transaction.
        Hibernate.initialize(existingMenu.getItems());
        for (Item item : existingMenu.getItems()) {
            Hibernate.initialize(item.getCategory());
        }

        return existingMenu; // Return the updated menu entity
    }

    /**
     * Finds or creates a MasterCategory by description.
     */
    private MasterCategory findOrCreateMasterCategory(String categoryDescription) {
        return masterCategoryRepository.findByCategoryDescription(categoryDescription)
                .orElseGet(() -> {
                    MasterCategory mc = new MasterCategory();
                    mc.setCategoryDescription(categoryDescription);
                    return masterCategoryRepository.save(mc);
                });
    }
}
