package com.menubyte.controller;

import com.menubyte.dto.ItemCreationRequest;
import com.menubyte.dto.ItemUpdateRequest;
import com.menubyte.entity.Category;
import com.menubyte.entity.Item;
import com.menubyte.entity.MasterCategory;
import com.menubyte.entity.MasterItem;
import com.menubyte.service.CategoryService;
import com.menubyte.service.ItemService;
import com.menubyte.service.MasterCategoryService;
import com.menubyte.service.MasterItemService;
import com.menubyte.service.MenuService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

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
    private final MasterItemService masterItemService;
    private final MasterCategoryService masterCategoryService;

    // Constructor injection for all services
    public ItemController(ItemService itemService, CategoryService categoryService, MenuService menuService,
                          MasterItemService masterItemService, MasterCategoryService masterCategoryService) {
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.menuService = menuService;
        this.masterItemService = masterItemService;
        this.masterCategoryService = masterCategoryService;
    }

    /**
     * Creates a new item for a specific business's menu, handling new or existing categories.
     * The request body uses ItemCreationRequest DTO to provide flexibility.
     *
     * @param businessId The ID of the business for which the item is being created.
     * @param request The DTO containing item details, category information (new or existing), and master item ID.
     * @return ResponseEntity with the created Item object and HTTP status 201 (Created).
     * @throws ResponseStatusException if validation fails or entities are not found/conflict.
     */
    @PostMapping(value = "/{businessId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Item> createItem(@PathVariable Long businessId, @RequestBody ItemCreationRequest request) {
        // --- 1. Basic Request Validation (from DTO) ---
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body cannot be null.");
        }
        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name cannot be empty.");
        }
        if (request.getItemPrice() == null || request.getItemPrice() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item price must be a positive number.");
        }
        if (request.getItemDiscount() == null || request.getItemDiscount() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item discount must be a non-negative number.");
        }


        // --- 2. Category Handling (New or Existing) ---
        Category category;

        if (request.getIsNewCategory() != null && request.getIsNewCategory()) {
            // Case A: Creating a new category for the current menu.
            // The service layer will check if this category already exists for the menu.
            String newCategoryDescription = request.getCategoryDescription();
            if (newCategoryDescription == null || newCategoryDescription.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New category description cannot be empty when creating a new category.");
            }

            // Find or Create MasterCategory with the same description
            MasterCategory linkedMasterCategory = masterCategoryService.findByCategoryDescription(newCategoryDescription)
                    .orElseGet(() -> {
                        MasterCategory mc = new MasterCategory();
                        mc.setCategoryDescription(newCategoryDescription);
                        return masterCategoryService.save(mc);
                    });

            // Create the new Category entity (transient state).
            // It will be linked to the Menu and saved/found by ItemService.
            category = new Category();
            category.setCategoryDescription(newCategoryDescription);
            category.setMasterCategory(linkedMasterCategory);

        } else {
            // Case B: Using an existing category.
            if (request.getCategoryId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Existing category ID is required when not creating a new category.");
            }
            // Fetch the existing category. ItemService will validate its menu ownership.
            category = categoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));

            if (category.getMasterCategory() == null) {
                // This scenario indicates potential data inconsistency if MasterCategory is always required.
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Existing category (ID: " + category.getId() + ") is missing its MasterCategory association.");
            }
        }

        // --- 3. MasterItem Handling (Optional) ---
        MasterItem masterItem = null;
        if (request.getMasterItemId() != null) {
            masterItem = masterItemService.getMasterItemById(request.getMasterItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master Item not found with ID: " + request.getMasterItemId()));
        }

        // --- 4. Construct Item Entity from DTO and resolved entities ---
        Item newItem = new Item();
        newItem.setItemName(request.getItemName());
        newItem.setItemDescription(request.getItemDescription());
        newItem.setItemPrice(request.getItemPrice());
        newItem.setItemDiscount(request.getItemDiscount());
        newItem.setItemImage(request.getItemImage());
        newItem.setVegOrNonVeg(request.getVegOrNonVeg());
        newItem.setItemAvailability(request.getItemAvailability() != null ? request.getItemAvailability() : true);
        newItem.setBestseller(request.getBestseller() != null ? request.getBestseller() : false);

        newItem.setCategory(category);      // Set the (potentially transient/unsaved for new) Category entity
        newItem.setMasterItem(masterItem);  // Set the resolved MasterItem entity (can be null)
        // --- 5. Save Item via Service ---
        // ItemService will handle all the complex logic:
        // - Finding/creating the Menu.
        // - Checking for/saving the Category (and linking to Menu).
        // - Saving the Item.
        Item createdItem = itemService.createItemForBusiness(businessId, newItem);
       Optional<MasterItem> masterItemCheck= masterItemService.getMasterItemByNameIgnoreCase(createdItem.getItemName());

        System.out.println("Item '" + createdItem.getItemName() + "' (ID: " + createdItem.getId() + ") created successfully.");
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }

    /**
     * Retrieves all items for a given business's menu.
     *
     * @param businessId The ID of the business.
     * @return ResponseEntity with a list of Items and HTTP status 200 (OK).
     */
    @GetMapping(value = "/by-business/{businessId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Item>> getItemsForBusiness(@PathVariable Long businessId) {
        List<Item> items = itemService.getItemsForBusiness(businessId);
        if (items.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(items);
    }

    /**
     * Retrieves a single item by its ID.
     *
     * @param itemId The ID of the item to retrieve.
     * @return ResponseEntity with the found Item object and HTTP status 200 (OK).
     * @throws ResponseStatusException if the item is not found.
     */
    @GetMapping(value = "/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Item> getItemById(@PathVariable Long itemId) {
        Item item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    /**
     * Updates an existing item.
     * The request body uses ItemUpdateRequest DTO for clarity and proper relationship handling.
     *
     * @param itemId The ID of the item to update.
     * @param request The DTO containing updated item details and the category ID.
     * @return ResponseEntity with the updated Item object and HTTP status 200 (OK).
     * @throws ResponseStatusException if the item is not found or other validation issues.
     */
    @PutMapping(value = "/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Item> updateItem(@PathVariable Long itemId, @RequestBody ItemUpdateRequest request) {
        // Basic validation for the DTO
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body cannot be null.");
        }
        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name cannot be empty.");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item price must be a positive number.");
        }
        if (request.getItemDiscount() == null || request.getItemDiscount() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item discount must be a non-negative number.");
        }
        if (request.getCategoryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID must be provided for item update.");
        }

        Item updated = itemService.updateItem(itemId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an item by its ID.
     *
     * @param itemId The ID of the item to delete.
     * @return ResponseEntity with a success message and HTTP status 204 (No Content).
     * @throws ResponseStatusException if the item is not found.
     */
    @DeleteMapping("/{itemId}")
    @Transactional
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}