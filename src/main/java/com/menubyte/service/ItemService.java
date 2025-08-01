package com.menubyte.service;

import com.menubyte.entity.Business;
import com.menubyte.entity.Category;
import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import com.menubyte.entity.MasterItem; // Make sure this is imported
import com.menubyte.repository.BusinessRepository;
import com.menubyte.repository.ItemRepository;
import com.menubyte.repository.MasterItemRepository; // Make sure this is imported and exists
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional; // Import for transactional methods

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final BusinessRepository businessRepository;
    private final MasterItemRepository masterItemRepository; // Correctly injected now

    public ItemService(ItemRepository itemRepository, BusinessRepository businessRepository, MasterItemRepository masterItemRepository) {
        this.itemRepository = itemRepository;
        this.businessRepository = businessRepository;
        this.masterItemRepository = masterItemRepository;
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
        // This check ensures consistency between the path variable businessId and the item's menu's business.
        // It relies on item.getMenu() already having its business populated by the controller or lazy loading.
        if (item.getMenu().getBusiness() == null || !item.getMenu().getBusiness().getId().equals(businessId)) {
            log.error("Item's menu (ID: {}) does not belong to the specified business ID: {}", item.getMenu().getId(), businessId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item's menu does not belong to this business.");
        }

        // MasterItem handling: If masterItem is linked (by ID) in the incoming Item object, fetch it
        // The controller should ideally fetch the MasterItem entity and set it on the 'item' object
        // before passing it here. If it only provides an ID, this logic fetches the full entity.
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
        // Using the new findByMenuBusinessId method from ItemRepository
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
     * Updates an existing item.
     * @param itemId Item ID.
     * @param updatedItem Updated item details.
     * @return Updated Item object.
     */
    @Transactional
    public Item updateItem(Long itemId, Item updatedItem) {
        log.info("Updating item with ID: {}", itemId);
        Item existingItem = getItemById(itemId); // Uses the service's own getItemById for existence check and error handling

        // Update basic fields
        existingItem.setItemName(updatedItem.getItemName());
        existingItem.setItemDescription(updatedItem.getItemDescription());
        existingItem.setItemPrice(updatedItem.getItemPrice());
        existingItem.setItemDiscount(updatedItem.getItemDiscount());
        existingItem.setItemImage(updatedItem.getItemImage());
        existingItem.setVegOrNonVeg(updatedItem.getVegOrNonVeg());
        existingItem.setItemAvailability(updatedItem.isItemAvailability());
        existingItem.setBestseller(updatedItem.isBestseller());

        // --- Category Update Logic (if allowed) ---
        // If the updatedItem's category ID is different, fetch and set the new category.
        // This assumes updatedItem.getCategory() will contain at least the ID if changing.
        if (updatedItem.getCategory() != null && updatedItem.getCategory().getId() != null) {
            if (existingItem.getCategory() == null || !existingItem.getCategory().getId().equals(updatedItem.getCategory().getId())) {
                // Fetch the new category from the database
                Category newCategory = existingItem.getMenu().getCategories().stream() // Get categories from the item's current menu
                        .filter(c -> c.getId().equals(updatedItem.getCategory().getId()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                "New category not found with ID: " + updatedItem.getCategory().getId() + " for this item's menu."));
                existingItem.setCategory(newCategory);
            }
        } else if (updatedItem.getCategory() == null) {
            // If the incoming updatedItem explicitly sets category to null, you might want to handle this
            // e.g., throw an error if a category is mandatory, or set existingItem.setCategory(null) if allowed.
            // Based on your Item entity, category_id is nullable = false, so this shouldn't be allowed.
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category cannot be null for an item.");
        }


        // --- MasterItem Update Logic (if allowed) ---
        // This assumes updatedItem.getMasterItem() might contain an ID if changing
        // You need to decide if MasterItem can be changed on an existing item.
        // If updatedItem.getMasterItem() is null, it means frontend explicitly wants to unlink master item.
        if (updatedItem.getMasterItem() != null && updatedItem.getMasterItem().getId() != null) {
            if (existingItem.getMasterItem() == null || !existingItem.getMasterItem().getId().equals(updatedItem.getMasterItem().getId())) {
                MasterItem newMasterItem = masterItemRepository.findById(updatedItem.getMasterItem().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master Item not found with ID: " + updatedItem.getMasterItem().getId()));
                existingItem.setMasterItem(newMasterItem);
            }
        } else if (updatedItem.getMasterItem() == null) {
            existingItem.setMasterItem(null); // Explicitly unlink if frontend sends null
        }


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