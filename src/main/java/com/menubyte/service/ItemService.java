/**
 * Service for managing Item entities.
 * Handles item creation, retrieval, and updates.
 *
 * @author Ankit Srivastava
 */
package com.menubyte.service;

import com.menubyte.entity.Business;
import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import com.menubyte.repository.BusinessRepository;
import com.menubyte.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final BusinessRepository businessRepository;

    public ItemService(ItemRepository itemRepository, BusinessRepository businessRepository) {
        this.itemRepository = itemRepository;
        this.businessRepository = businessRepository;
    }

    /**
     * Create an item for a business's menu.
     * @param businessId Business ID.
     * @param item Item details.
     * @return Created Item object.
     */
    public Item createItemForBusiness(Long businessId, Item item) {
        log.info("Creating item for business ID: {}", businessId);
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> {
                    log.error("Business not found with ID: {}", businessId);
                    return new RuntimeException("Business not found");
                });

        Menu menu = business.getMenu();
        if (menu == null) {
            log.error("Menu not found for business ID: {}", businessId);
            throw new RuntimeException("Menu not found for this business");
        }

        item.setMenu(menu);
        Item savedItem = itemRepository.save(item);
        log.info("Item created successfully: {}", savedItem);
        return savedItem;
    }

    /**
     * Get all items for a business's menu.
     * @param businessId Business ID.
     * @return List of Items.
     */
    public List<Item> getItemsForBusiness(Long businessId) {
        log.info("Fetching items for business ID: {}", businessId);
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> {
                    log.error("Business not found with ID: {}", businessId);
                    return new RuntimeException("Business not found");
                });

        Menu menu = business.getMenu();
        if (menu == null) {
            log.error("Menu not found for business ID: {}", businessId);
            throw new RuntimeException("Menu not found for this business");
        }

        List<Item> items = itemRepository.findByMenu(menu);
        log.info("Total items found for business {}: {}", businessId, items.size());
        return items;
    }

    /**
     * Update an existing item.
     * @param itemId Item ID.
     * @param updatedItem Updated item details.
     * @return Updated Item object.
     */
    public Item updateItem(Long itemId, Item updatedItem) {
        log.info("Updating item with ID: {}", itemId);
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with ID: {}", itemId);
                    return new RuntimeException("Item not found");
                });

        existingItem.setItemName(updatedItem.getItemName());
        existingItem.setItemDescription(updatedItem.getItemDescription());
        existingItem.setItemPrice(updatedItem.getItemPrice());
        existingItem.setItemDiscount(updatedItem.getItemDiscount());
        existingItem.setItemImage(updatedItem.getItemImage());
        existingItem.setVegOrNonVeg(updatedItem.getVegOrNonVeg());
        existingItem.setItemAvailability(updatedItem.isItemAvailability());
        existingItem.setBestseller(updatedItem.isBestseller());
        existingItem.setCategory(updatedItem.getCategory());

        Item updated = itemRepository.save(existingItem);
        log.info("Item updated successfully: {}", updated);
        return updated;
    }
}
