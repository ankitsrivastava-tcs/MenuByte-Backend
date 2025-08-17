package com.menubyte.controller;

import com.menubyte.dto.BulkItemCreationRequest;
import com.menubyte.dto.ItemCreationRequest;
import com.menubyte.dto.ItemUpdateRequest;
import com.menubyte.dto.ItemVariantDto;
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
    public ResponseEntity<Item> createItem(@PathVariable Long businessId, @RequestBody ItemCreationRequest request) {
        Item createdItem=itemService.createItemForBusiness(businessId,request);
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

        // CORRECTED: Validate the list of variants instead of a single price
        List<ItemVariantDto> variants = request.getVariants();
        if (variants == null || variants.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item must have at least one price variant.");
        }
        for (ItemVariantDto variant : variants) {
            if (variant.getVariantName() == null || variant.getVariantName().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All price variants must have a name.");
            }
            if (variant.getPrice() == null || variant.getPrice() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All price variants must have a positive price.");
            }
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
    /**
     * Creates multiple new items for a specific business's menu in a single request.
     * The request body contains a list of ItemCreationRequest objects.
     */
    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<List<Item>> createBulkItems(@RequestBody BulkItemCreationRequest request) {
        List<Item> createdItems = itemService.createBulkItemsForBusiness(request.getBusinessId(), request.getItems());
        return new ResponseEntity<>(createdItems, HttpStatus.CREATED);
    }
}