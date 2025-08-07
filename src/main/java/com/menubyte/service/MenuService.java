package com.menubyte.service;

import com.menubyte.dto.CategoryDTO;
import com.menubyte.dto.ItemDTO;
import com.menubyte.dto.MenuDTO;
import com.menubyte.entity.*;
import com.menubyte.mapper.ItemMapper;
import com.menubyte.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException; // Import ResponseStatusException
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final BusinessRepository businessRepository;
    private final BusinessMasterService businessMasterService;

    // private final CategoryRepository categoryRepository; // Removed if not directly used here
    // private final MasterCategoryRepository masterCategoryRepository; // Removed if not used by public methods
    private final ItemRepository itemRepository;

    public MenuService(MenuRepository menuRepository,
                       BusinessRepository businessRepository,
                       CategoryRepository categoryRepository, // Keep if still injected for other methods
                       MasterCategoryRepository masterCategoryRepository, // Keep if still injected for other methods
                       ItemRepository itemRepository,BusinessMasterService businessMasterService) {
        this.menuRepository = menuRepository;
        this.businessRepository = businessRepository;
        // this.categoryRepository = categoryRepository; // Keep if still injected for other methods
        // this.masterCategoryRepository = masterCategoryRepository; // Keep if still injected for other methods
        this.itemRepository = itemRepository;
        this.businessMasterService=businessMasterService;
    }

    /**
     * Get Menu for a User's Business.
     */
    public Menu getMenuForUserBusiness(Long businessId, User user) {
        log.info("Fetching menu for business ID: {} and user ID: {}", businessId, user.getId());
        businessMasterService.getBusinessesByBusinessID(businessId);
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> {
                    log.error("Business not found with ID: {}", businessId);
                    // Use ResponseStatusException for better API error handling
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found with ID: " + businessId);
                });

        if (!business.getUser().getId().equals(user.getId())) {
            log.error("Unauthorized access attempt by user ID: {} for business ID: {}", user.getId(), businessId);
            // Use ResponseStatusException for better API error handling
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unauthorized! You don't own this business.");
        }

        return menuRepository.findByBusinessId(businessId)
                .orElseThrow(() -> {
                    log.error("No menu found for business ID: {}", businessId);
                    // Use ResponseStatusException for better API error handling
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "No menu found for this business.");
                });
    }

    /**
     * Find Menu by Business ID.
     * This method is used by ItemService to check for existing menus or create new ones.
     */
    public Optional<Menu> findByBusinessId(Long businessId) {
        log.info("Finding menu for business ID: {}", businessId);
        return menuRepository.findByBusinessId(businessId);
    }

    /**
     * Saves a new Menu entity.
     * This method is called from ItemService when a default menu needs to be created.
     * @param menu The Menu entity to save.
     * @return The saved Menu entity.
     */
    @Transactional // Ensure the save operation is transactional
    public Menu save(Menu menu) {
        log.info("Saving new menu with name: {}", menu.getMenuName());
        return menuRepository.save(menu);
    }


    /**
     * Updates the items of a menu for a given business and user.
     * This method is designed to update *existing* items within *existing* categories.
     * It does not support creating new categories or new items.
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
                log.warn("Skipping category update: CategoryDTO for '{}' has no ID. This method only updates existing categories.", categoryDto.getCategoryName());
                continue; // Skip categories without an ID (i.e., new categories)
            }

            // We might need to find the existing Category entity if the DTO only sends ID
            // For now, assuming categoryDto.getId() is sufficient for item lookup.
            // If you need to update Category properties as well, fetch the Category entity:
            // Optional<Category> existingCategoryOptional = categoryRepository.findById(categoryDto.getId());
            // if (existingCategoryOptional.isPresent()) { Category existingCategory = existingCategoryOptional.get(); ... }

            for (ItemDTO itemDto : categoryDto.getItems()) {
                if (itemDto.getId() == null) {
                    log.warn("Skipping item update: ItemDTO for '{}' has no ID. This method only updates existing items.", itemDto.getItemName());
                    continue; // Skip items without an ID (i.e., new items)
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
                    Hibernate.initialize(existingItem.getMenu()); // Also initialize menu if you access its properties after this method

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
            Hibernate.initialize(item.getMenu());
            // Also initialize masterItem if it's accessed (and can be lazy loaded)
            if (item.getMasterItem() != null) {
                Hibernate.initialize(item.getMasterItem());
            }
        }

        return existingMenu; // Return the updated menu entity
    }

    /**
     * Finds or creates a MasterCategory by description.
     * This method is private and not currently called by any public method in MenuService.
     * It might be intended for future use or belong in CategoryService.
     */
    private MasterCategory findOrCreateMasterCategory(String categoryDescription) {
        // This method will only work if masterCategoryRepository is still injected.
        // Consider moving this logic to CategoryService if it's related to Category management.
        return null; // Placeholder: replace with actual logic if keeping this method and its dependency
        /*
        return masterCategoryRepository.findByCategoryDescription(categoryDescription)
                .orElseGet(() -> {
                    MasterCategory mc = new MasterCategory();
                    mc.setCategoryDescription(categoryDescription);
                    return masterCategoryRepository.save(mc);
                });
        */
    }
}