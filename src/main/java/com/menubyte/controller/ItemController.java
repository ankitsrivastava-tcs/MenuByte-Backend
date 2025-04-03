package com.menubyte.controller;

import com.menubyte.entity.Item;
import com.menubyte.entity.Category;
import com.menubyte.entity.Menu;
import com.menubyte.service.ItemService;
import com.menubyte.service.CategoryService;
import com.menubyte.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing menu items within a business.
 * Handles CRUD operations for menu items.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;
    private final CategoryService categoryService;
    private final MenuService menuService;

    public ItemController(ItemService itemService, CategoryService categoryService, MenuService menuService) {
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.menuService = menuService;
    }

    /**
     * Creates a new item for a specific business's menu.
     */
    @PostMapping("/{businessId}")
    public ResponseEntity<?> createItem(@PathVariable Long businessId, @RequestBody Item item) {
        if (item == null || item.getItemName() == null) {
            return ResponseEntity.badRequest().body("Invalid item data provided.");
        }

        // ✅ Unwrap Optional properly
        Category category = categoryService.findById(item.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Menu menu = menuService.findByBusinessId(businessId)
                .orElseThrow(() -> new RuntimeException("Menu not found for business ID: " + businessId));

        // ✅ Set category and menu before saving
        item.setCategory(category);
        item.setMenu(menu);

        Item createdItem = itemService.createItemForBusiness(businessId, item);
        return ResponseEntity.ok(createdItem);
    }

    /**
     * Retrieves all items for a given business's menu.
     */
    @GetMapping("/{businessId}")
    public ResponseEntity<List<Item>> getItemsForBusiness(@PathVariable Long businessId) {
        List<Item> items = itemService.getItemsForBusiness(businessId);
        return ResponseEntity.ok(items);
    }

    /**
     * Retrieves an item by its ID.
     */
    @GetMapping("/item/{itemId}")
    public ResponseEntity<?> getItemById(@PathVariable Long itemId) {
        Item item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    /**
     * Updates an existing item.
     */
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId, @RequestBody Item updatedItem) {
        Item updated = itemService.updateItem(itemId, updatedItem);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an item by its ID.
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok("Item deleted successfully.");
    }
}
