/**
 * Service for managing Item entities.
 * Handles item creation, retrieval, updates, and deletion.
 *
 * @author Ankit Srivastava
 */
package com.menubyte.service;

import com.menubyte.entity.Business;
import com.menubyte.entity.Category;
import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import com.menubyte.repository.BusinessRepository;
import com.menubyte.repository.ItemRepository;
import com.menubyte.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final BusinessRepository businessRepository;
    private final CategoryRepository categoryRepository;

    public ItemService(ItemRepository itemRepository, BusinessRepository businessRepository, CategoryRepository categoryRepository) {
        this.itemRepository = itemRepository;
        this.businessRepository = businessRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Creates an item for a business's menu.
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

        Category category = categoryRepository.findById(item.getCategory().getId())
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", item.getCategory().getId());
                    return new RuntimeException("Category not found");
                });

        item.setMenu(menu);
        item.setCategory(category);
        Item savedItem = itemRepository.save(item);
        log.info("Item created successfully: {}", savedItem);
        return savedItem;
    }

    /**
     * Retrieves all items for a business's menu.
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
     * Retrieves an item by its ID.
     * @param itemId The ID of the item to retrieve.
     * @return The found item.
     */
    public Item getItemById(Long itemId) {
        log.info("Fetching item with ID: {}", itemId);
        return itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item not found with ID: {}", itemId);
                    return new RuntimeException("Item not found");
                });
    }

    /**
     * Updates an existing item.
     * @param itemId Item ID.
     * @param updatedItem Updated item details.
     * @return Updated Item object.
     */
    public Item updateItem(Long itemId, Item updatedItem) {
        log.info("Updating item with ID: {}", itemId);
        Item existingItem = getItemById(itemId);

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

    /**
     * Deletes an item by its ID.
     * @param itemId Item ID.
     */
    public void deleteItem(Long itemId) {
        log.info("Deleting item with ID: {}", itemId);
        Item item = getItemById(itemId);
        itemRepository.delete(item);
        log.info("Item deleted successfully.");
    }
}
