package com.menubyte.controller;

import com.menubyte.entity.Item;
import com.menubyte.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing menu items within a business.
 * Handles CRUD operations for menu items.
 *
 * @author Ankit
 * @version 1.0
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    /**
     * Constructor to initialize ItemController with ItemService.
     *
     * @param itemService Service layer for handling item-related operations
     */
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Creates a new item for a specific business's menu.
     *
     * @param businessId The ID of the business to which the item belongs
     * @param item       The item object containing item details
     * @return ResponseEntity containing the created item
     */
    @PostMapping("/{businessId}")
    public ResponseEntity<Item> createItem(@PathVariable Long businessId, @RequestBody Item item) {
        Item createdItem = itemService.createItemForBusiness(businessId, item);
        return ResponseEntity.ok(createdItem);
    }

    /**
     * Retrieves all items for a given business's menu.
     *
     * @param businessId The ID of the business whose menu items are to be retrieved
     * @return ResponseEntity containing a list of items
     */
    @GetMapping("/{businessId}")
    public ResponseEntity<List<Item>> getItemsForBusiness(@PathVariable Long businessId) {
        List<Item> items = itemService.getItemsForBusiness(businessId);
        return ResponseEntity.ok(items);
    }

    /**
     * Updates an existing item in a business's menu.
     *
     * @param itemId      The ID of the item to be updated
     * @param updatedItem The updated item details
     * @return ResponseEntity containing the updated item
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<Item> updateItem(@PathVariable Long itemId, @RequestBody Item updatedItem) {
        Item item = itemService.updateItem(itemId, updatedItem);
        return ResponseEntity.ok(item);
    }
}