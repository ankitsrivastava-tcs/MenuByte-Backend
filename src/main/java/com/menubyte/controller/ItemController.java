package com.menubyte.controller;

import com.menubyte.dto.ItemCreationRequest;
import com.menubyte.dto.ItemUpdateRequest;
import com.menubyte.entity.Category;
import com.menubyte.entity.Item;
import com.menubyte.entity.MasterCategory; // Assuming this entity exists
import com.menubyte.entity.MasterItem;
import com.menubyte.entity.Menu;
import com.menubyte.service.CategoryService;
import com.menubyte.service.ItemService;
import com.menubyte.service.MasterCategoryService; // Assuming this service exists
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
    private final MasterItemService masterItemService; // Make sure this is correctly injected if used
    private final MasterCategoryService masterCategoryService; // Inject MasterCategoryService

    // Constructor injection for all services
    public ItemController(ItemService itemService, CategoryService categoryService, MenuService menuService,
                          MasterItemService masterItemService, MasterCategoryService masterCategoryService) { // Added MasterCategoryService here
        this.itemService = itemService;
        this.categoryService = categoryService;
        this.menuService = menuService;
        this.masterItemService = masterItemService; // This is now correctly handled
        this.masterCategoryService = masterCategoryService; // Initialize MasterCategoryService
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
    @Transactional // Ensures atomicity for database operations within this method
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

        // --- 2. Fetch Menu Entity ---
        // Ensure the business has an active menu.
        // We rely on the businessId from the path, and potentially validate against menuId from request if present.
        Menu menu = menuService.findByBusinessId(businessId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Menu not found for business ID: " + businessId + ". Please create a menu first."));

        // If the request DTO also sends menuId, it's good to validate consistency
        if (request.getMenuId() != null && !request.getMenuId().equals(menu.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Provided menu ID in request (" + request.getMenuId() + ") does not match the active menu for business ID " + businessId + " (ID: " + menu.getId() + ").");
        }


        // --- 3. Category Handling (New or Existing) ---
        Category category;
        MasterCategory linkedMasterCategory = null; // To store the MasterCategory linked to the Category

        if (request.getIsNewCategory() != null && request.getIsNewCategory()) {
            // Case A: Create a new category and its corresponding MasterCategory
            String newCategoryDescription = request.getCategoryDescription();
            if (newCategoryDescription == null || newCategoryDescription.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New category description cannot be empty when creating a new category.");
            }

            // Find or Create MasterCategory with the same description
            linkedMasterCategory = masterCategoryService.findByCategoryDescription(newCategoryDescription)
                    .orElseGet(() -> {
                        MasterCategory mc = new MasterCategory();
                        mc.setCategoryDescription(newCategoryDescription);
                        return masterCategoryService.save(mc);
                    });

            // Create the new Category entity and link it to the Menu and the (new/existing) MasterCategory
            category = new Category();
            category.setCategoryDescription(newCategoryDescription);
            category.setMenu(menu);
            category.setMasterCategory(linkedMasterCategory); // Set the MasterCategory
            category = categoryService.saveCategory(category); // Save the new Category
            System.out.println("New Category created: " + category.getCategoryDescription() + " (ID: " + category.getId() + ") linked to MasterCategory ID: " + linkedMasterCategory.getId());

        } else {
            // Case B: Use an existing category
            if (request.getCategoryId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Existing category ID is required when not creating a new category.");
            }
            category = categoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));

            // Verify that the fetched category belongs to the menu
            if (category.getMenu() == null || !category.getMenu().getId().equals(menu.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category with ID " + request.getCategoryId() + " does not belong to the specified Menu (ID: " + menu.getId() + ").");
            }
            // Ensure existing category has a MasterCategory linked as per business rule
            if (category.getMasterCategory() == null) {
                // This indicates data inconsistency if all categories MUST have a master category.
                // Depending on your system's strictness, you might auto-assign or throw an error.
                // For now, let's assume it should always be present if the data is clean.
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Existing category (ID: " + category.getId() + ") is missing its MasterCategory association.");
            }
            linkedMasterCategory = category.getMasterCategory(); // Get the existing master category
        }

        // --- 4. MasterItem Handling (Optional) ---
        MasterItem masterItem = null;
        if (request.getMasterItemId() != null) {
            masterItem = masterItemService.getMasterItemById(request.getMasterItemId()) // Use the service's method
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master Item not found with ID: " + request.getMasterItemId()));
        }

        // --- 5. Construct Item Entity from DTO and resolved entities ---
        Item newItem = new Item();
        newItem.setItemName(request.getItemName());
        newItem.setItemDescription(request.getItemDescription());
        newItem.setItemPrice(request.getItemPrice());
        newItem.setItemDiscount(request.getItemDiscount());
        newItem.setItemImage(request.getItemImage());
        newItem.setVegOrNonVeg(request.getVegOrNonVeg());
        // Use default values if availability/bestseller are null in DTO
        newItem.setItemAvailability(request.getItemAvailability() != null ? request.getItemAvailability() : true);
        newItem.setBestseller(request.getBestseller() != null ? request.getBestseller() : false);

        newItem.setCategory(category);      // Set the resolved Category entity
        newItem.setMenu(menu);              // Set the resolved Menu entity (important for relationship)
        newItem.setMasterItem(masterItem);  // Set the resolved MasterItem entity (can be null)

        // --- 6. Save Item via Service ---
        // The service will handle setting createdDate/updatedDate if JPA Auditing is enabled.
        Item createdItem = itemService.createItemForBusiness(businessId, newItem);
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
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Return 204 if no items
        }
        return ResponseEntity.ok(items);
    }

    /**
     * Retrieves a single item by its ID.
     *
     * @param itemId The ID of the item to retrieve.
     * @return ResponseEntity with the found Item object and HTTP status 200 (OK).
     * @throws ResponseStatusException if the item is not found (handled by ItemService).
     */
    @GetMapping(value = "/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE) // Changed mapping for clarity
    public ResponseEntity<Item> getItemById(@PathVariable Long itemId) {
        // The service layer should handle the Optional.orElseThrow for not found
        Item item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    /**
     * Updates an existing item.
     * The request body now uses ItemUpdateRequest DTO for clarity and proper relationship handling.
     *
     * @param itemId The ID of the item to update.
     * @param request The DTO containing updated item details and the category ID.
     * @return ResponseEntity with the updated Item object and HTTP status 200 (OK).
     * @throws ResponseStatusException if the item is not found or other validation issues.
     */
    @PutMapping(value = "/{itemId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<Item> updateItem(@PathVariable Long itemId, @RequestBody ItemUpdateRequest request) { // <-- Changed
        // Basic validation for the DTO
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body cannot be null.");
        }
        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name cannot be empty.");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) { // Using getPrice
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item price must be a positive number.");
        }
        if (request.getItemDiscount() == null || request.getItemDiscount() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item discount must be a non-negative number.");
        }
        if (request.getCategoryId() == null) { // Validate categoryId is provided
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category ID must be provided for item update.");
        }

        Item updated = itemService.updateItem(itemId, request); // <-- Changed
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an item by its ID.
     *
     * @param itemId The ID of the item to delete.
     * @return ResponseEntity with a success message and HTTP status 204 (No Content).
     * @throws ResponseStatusException if the item is not found (handled by ItemService).
     */
    @DeleteMapping("/{itemId}")
    @Transactional
    public ResponseEntity<Void> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId); // Service throws NOT_FOUND if not found
        return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content for successful deletion
    }
}