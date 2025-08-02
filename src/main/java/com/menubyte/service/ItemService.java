package com.menubyte.service;

import com.menubyte.dto.ItemUpdateRequest;
import com.menubyte.entity.Business;
import com.menubyte.entity.Category;
import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import com.menubyte.entity.MasterItem;
import com.menubyte.repository.BusinessRepository;
import com.menubyte.repository.ItemRepository;
import com.menubyte.repository.MasterItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final BusinessRepository businessRepository;
    private final MasterItemRepository masterItemRepository;
    private final CategoryService categoryService;
    private final MenuService menuService;

    public ItemService(ItemRepository itemRepository, BusinessRepository businessRepository,
                       MasterItemRepository masterItemRepository, CategoryService categoryService,
                       MenuService menuService) {
        this.itemRepository = itemRepository;
        this.businessRepository = businessRepository;
        this.masterItemRepository = masterItemRepository;
        this.categoryService = categoryService;
        this.menuService = menuService;
    }

    @Transactional
    public Item createItemForBusiness(Long businessId, Item item) {
        log.info("Creating item for business ID: {}. Item name: {}", businessId, item.getItemName());

        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> {
                    log.error("Business not found with ID: {}", businessId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found with ID: " + businessId);
                });

        Menu menu = menuService.findByBusinessId(businessId)
                .orElseGet(() -> {
                    log.info("No existing menu found for business ID: {}. Creating a default menu.", businessId);
                    Menu newMenu = new Menu();
                    newMenu.setMenuName("Default Menu for " + business.getBusinessName());
                    newMenu.setBusiness(business);
                    if (newMenu.getCreatedDate() == null) {
                        newMenu.setCreatedDate(LocalDateTime.now());
                    }
                    newMenu.setUpdatedDate(LocalDateTime.now());
                    return menuService.save(newMenu);
                });

        Category category = item.getCategory();

        if (category == null) {
            log.error("Item's category is null before processing.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category information is missing for the item.");
        }

        if (category.getId() == null) {
            // This means it's a NEW Category (transient) from the controller,
            // which has its description and MasterCategory set.
            log.info("Incoming category has no ID, assuming new category. Delegating creation to CategoryService.");

            // *** CRITICAL CHANGE HERE: Use the dedicated createCategory method from CategoryService ***
            // This method handles the uniqueness check for categoryDescription within the menu,
            // and saving the category. It will throw CONFLICT if a duplicate exists.
            try {
                category = categoryService.createCategory(category.getCategoryDescription(), menu);
                log.info("New/Existing Category resolved by CategoryService. Category ID: {}", category.getId());
            } catch (ResponseStatusException e) {
                // If CategoryService's createCategory throws CONFLICT, re-throw it.
                // Or handle other exceptions like BAD_REQUEST if relevant.
                log.error("Failed to create/resolve category via CategoryService: {}", e.getReason());
                throw e; // Re-throw the exception from CategoryService
            }
        } else {
            // This is an EXISTING category by ID. Fetch it to ensure it's managed and valid.
            Optional<Category> existingCategoryOpt = categoryService.findById(category.getId());
            if (existingCategoryOpt.isEmpty()) {
                log.error("Existing category with ID {} not found.", category.getId());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Existing category not found with ID: " + category.getId());
            }
            category = existingCategoryOpt.get(); // Use the managed entity

            // Validate that the existing category belongs to this business's menu
            if (category.getMenu() == null || !category.getMenu().getId().equals(menu.getId())) {
                log.error("Existing category ID {} does not belong to menu ID {} for business ID {}",
                        category.getId(), menu.getId(), businessId);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Existing category with ID " + category.getId() + " does not belong to the menu of business ID " + businessId + ".");
            }
            log.info("Using existing category with ID: {}", category.getId());
        }
        item.setCategory(category);


        item.setMenu(menu);

        if (item.getMasterItem() != null && item.getMasterItem().getId() != null) {
            MasterItem masterItem = masterItemRepository.findById(item.getMasterItem().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master Item not found with ID: " + item.getMasterItem().getId()));
            item.setMasterItem(masterItem);
        } else {
            item.setMasterItem(null);
        }

        if (item.getCreatedDate() == null) {
            item.setCreatedDate(LocalDateTime.now());
        }
        item.setUpdatedDate(LocalDateTime.now());

        Item savedItem = itemRepository.save(item);
        log.info("Item created successfully with ID: {} and linked to menu ID: {}", savedItem.getId(), menu.getId());
        return savedItem;
    }

    // ... (rest of ItemService methods like getItemsForBusiness, getItemById, updateItem, deleteItem) ...

    /**
     * Retrieves all items for a business's menu.
     * @param businessId Business ID.
     * @return List of Items.
     */
    public List<Item> getItemsForBusiness(Long businessId) {
        log.info("Fetching items for business ID: {}", businessId);
        List<Item> items = itemRepository.findByMenuBusinessId(businessId);
        log.info("Total items found for business {}: {}", businessId, items.size());
        return items;
    }

    /**
     * Retrieves an item by its ID.
     * @param itemId The ID of the item to retrieve.
     * @return The found item.
     */
    public Item getItemById(Long itemId) {
        log.info("Fetching item with ID: {}", itemId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with ID: {}", itemId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found with ID: " + itemId);
                });
    }

    /**
     * Updates an existing item using an ItemUpdateRequest DTO.
     * @param itemId Item ID.
     * @param request DTO containing updated item details and category ID.
     * @return Updated Item object.
     */
    @Transactional
    public Item updateItem(Long itemId, ItemUpdateRequest request) {
        log.info("Updating item with ID: {}", itemId);
        Item existingItem = getItemById(itemId);

        // Fetch the Category entity using the ID from the DTO
        Category newCategory = categoryService.findById(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Category not found with ID: " + request.getCategoryId()));

        // --- Important: Ensure the fetched category belongs to the item's menu ---
        if (existingItem.getMenu() == null || newCategory.getMenu() == null || !newCategory.getMenu().getId().equals(existingItem.getMenu().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Category (ID: " + newCategory.getId() + ") does not belong to the menu of item (ID: " + itemId + ").");
        }

        // Update basic fields from the DTO
        existingItem.setItemName(request.getItemName());
        existingItem.setItemDescription(request.getItemDescription());
        existingItem.setItemPrice(request.getPrice());
        existingItem.setItemDiscount(request.getItemDiscount());
        existingItem.setItemImage(request.getItemImage());
        existingItem.setVegOrNonVeg(request.getVegOrNonVeg());

        // Handle null Booleans from DTO for primitive boolean fields
        existingItem.setItemAvailability(request.getItemAvailability() != null ? request.getItemAvailability() : false);
        existingItem.setBestseller(request.getBestseller() != null ? request.getBestseller() : false);

        // Set the resolved Category entity
        existingItem.setCategory(newCategory);

        // Update timestamp
        existingItem.setUpdatedDate(LocalDateTime.now());

        Item updated = itemRepository.save(existingItem);
        log.info("Item updated successfully with ID: {}", updated.getId());
        return updated;
    }

    /**
     * Deletes an item by its ID.
     * @param itemId Item ID.
     */
    @Transactional
    public void deleteItem(Long itemId) {
        log.info("Deleting item with ID: {}", itemId);
        Item item = getItemById(itemId);
        itemRepository.delete(item);
        log.info("Item with ID: {} deleted successfully.", itemId);
    }
}