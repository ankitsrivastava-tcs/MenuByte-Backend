package com.menubyte.service;

import com.menubyte.dto.ItemUpdateRequest; // <--- NEW: Import the DTO
import com.menubyte.entity.Business;
import com.menubyte.entity.Category;
import com.menubyte.entity.Item;
import com.menubyte.entity.Menu; // Unused, but keep if needed elsewhere
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
    private final CategoryService categoryService; // <--- NEW: Inject CategoryService

    public ItemService(ItemRepository itemRepository, BusinessRepository businessRepository,
                       MasterItemRepository masterItemRepository, CategoryService categoryService) { // <--- NEW: Add to constructor
        this.itemRepository = itemRepository;
        this.businessRepository = businessRepository;
        this.masterItemRepository = masterItemRepository;
        this.categoryService = categoryService; // <--- NEW: Initialize
    }

    /**
     * Creates an item for a business's menu.
     * This method expects the Item object to already have its associated
     * Category and Menu entities set by the controller.
     *
     * @param businessId Business ID (for verification).
     * @param item Item details with resolved Category, Menu, and potentially MasterItem.
     * @return Created Item object.
     */
    @Transactional // Ensure atomicity for database operations
    public Item createItemForBusiness(Long businessId, Item item) {
        log.info("Creating item for business ID: {}. Item name: {}", businessId, item.getItemName());

        // Sanity check: Verify that the business exists.
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> {
                    log.error("Business not found with ID: {}", businessId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found with ID: " + businessId);
                });

        // Ensure Menu and Category are properly set by the controller
        if (item.getMenu() == null || item.getMenu().getId() == null) {
            log.error("Item's menu is null or missing ID after controller processing.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Menu information not properly set for item.");
        }
        if (item.getCategory() == null || item.getCategory().getId() == null) {
            log.error("Item's category is null or missing ID after controller processing.");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Category information not properly set for item.");
        }

        // Verify that the menu linked to the item actually belongs to the provided businessId
        if (item.getMenu().getBusiness() == null || !item.getMenu().getBusiness().getId().equals(businessId)) {
            log.error("Item's menu (ID: {}) does not belong to the specified business ID: {}", item.getMenu().getId(), businessId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item's menu does not belong to this business.");
        }

        // MasterItem handling: If masterItem is linked (by ID) in the incoming Item object, fetch it
        if (item.getMasterItem() != null && item.getMasterItem().getId() != null) {
            MasterItem masterItem = masterItemRepository.findById(item.getMasterItem().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master Item not found with ID: " + item.getMasterItem().getId()));
            item.setMasterItem(masterItem);
        } else {
            item.setMasterItem(null); // Ensure it's null if not provided or unlinked
        }

        // Set creation/update timestamps
        item.setCreatedDate(LocalDateTime.now());
        item.setUpdatedDate(LocalDateTime.now());

        Item savedItem = itemRepository.save(item);
        log.info("Item created successfully with ID: {}", savedItem.getId());
        return savedItem;
    }

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
        Item existingItem = getItemById(itemId); // Fetch existing item

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

        // --- FIX: Handle null Booleans from DTO for primitive boolean fields ---
        existingItem.setItemAvailability(request.getItemAvailability() != null ? request.getItemAvailability() : false); // Provide a default if null
        existingItem.setBestseller(request.getBestseller() != null ? request.getBestseller() : false); // Provide a default if null

        // Set the resolved Category entity
        existingItem.setCategory(newCategory);

        // --- MasterItem Update Logic (Optional - if you want to allow changing master item) ---
        // Uncomment and adapt if your frontend sends masterItemId in ItemUpdateRequest
        /*
        if (request.getMasterItemId() != null) {
            MasterItem newMasterItem = masterItemRepository.findById(request.getMasterItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master Item not found with ID: " + request.getMasterItemId()));
            existingItem.setMasterItem(newMasterItem);
        } else {
            existingItem.setMasterItem(null); // Explicitly unlink if DTO sends null or doesn't include it
        }
        */

        existingItem.setUpdatedDate(LocalDateTime.now()); // Update timestamp

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
        Item item = getItemById(itemId); // Uses the service's own getItemById for existence check and error handling
        itemRepository.delete(item);
        log.info("Item with ID: {} deleted successfully.", itemId);
    }
}