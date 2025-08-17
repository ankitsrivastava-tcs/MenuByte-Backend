package com.menubyte.service;

import com.menubyte.dto.ItemCreationRequest;
import com.menubyte.dto.ItemUpdateRequest;
import com.menubyte.dto.ItemVariantDto;
import com.menubyte.entity.*;
import com.menubyte.repository.*;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemService {
@Autowired
private  ItemRepository itemRepository;
    @Autowired

    private  BusinessRepository businessRepository;
    @Autowired

    private  MasterItemRepository masterItemRepository;
    @Autowired
    @Lazy

    private  CategoryService categoryService;
    @Autowired

    private  MenuService menuService;
    @Autowired

    private  MasterCategoryRepository masterCategoryRepository;
    @Autowired
    private  MasterCategoryService masterCategoryService;
    @Autowired
    private  BusinessService businessService;
    @Autowired
    MasterItemService masterItemService;
    @Autowired
    ItemVariantRepository itemVariantRepository;
        public Item createItemForBusiness(Long businessId, ItemCreationRequest request) {
        log.info("Starting item creation for business ID: {}", businessId);

        validateItemCreationRequest(request);

        Business business = businessService.getBusinessById(businessId);
        Menu menu = findOrCreateMenuForBusiness(businessId, business);
        Category category = getOrCreateCategory(request, business, menu); // Pass menu to this method
        MasterItem masterItem = getOrCreateMasterItem(request,category.getMasterCategory().getId(),category);

        Item newItem = mapRequestToNewItem(request, category, masterItem, menu);

        // Final check to ensure the category belongs to the menu
        if (category.getMenu() != null && !category.getMenu().getId().equals(menu.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Selected category does not belong to the menu of business ID " + businessId + ".");
        }

        Item savedItem = itemRepository.save(newItem);

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ItemVariant> variants = request.getVariants().stream()
                    .map(variantDto -> {
                        ItemVariant variant = new ItemVariant();
                        variant.setVariantName(variantDto.getVariantName());
                        variant.setPrice(variantDto.getPrice());
                        variant.setItem(savedItem); // Link the variant to the item
                        return variant;
                    })
                    .collect(Collectors.toList());
            savedItem.setVariants(variants); // Add variants to the item entity
            //itemVariantRepository.saveAll(variants); // Save all variants
        }


        log.info("Item created successfully with ID: {} for business ID: {}", savedItem.getId(), businessId);
        return savedItem;
    }

    // (validateItemCreationRequest method remains the same)

    /**
     * Determines if a new category needs to be created or an existing one used.
     * This method now handles all category-related logic, including checking for duplicates.
     */
    private Category getOrCreateCategory(ItemCreationRequest request, Business business, Menu menu) {
        if (request.getIsNewCategory() != null && request.getIsNewCategory()) {
            if (request.getCategoryDescription() == null || request.getCategoryDescription().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New category requires a description.");
            }

            // --- CRITICAL CHANGE HERE ---
            // First, try to find an existing category with the same description for the menu
            Optional<Category> existingCategoryOpt = categoryService.findByMenuAndCategoryDescription(menu, request.getCategoryDescription());
            if (existingCategoryOpt.isPresent()) {
                log.info("Category with description '{}' already exists for menu ID {}. Using existing category.",
                        request.getCategoryDescription(), menu.getId());
                return existingCategoryOpt.get();
            }

            // If it doesn't exist, proceed with creating a new one
            MasterCategory linkedMasterCategory = masterCategoryService
                    .findByCategoryDescription(request.getCategoryDescription())
                    .orElseGet(() -> {
                        MasterCategory mc = new MasterCategory();
                        mc.setCategoryDescription(request.getCategoryDescription());
                        mc.setBusinessType(business.getBusinessType());
                        return masterCategoryService.save(mc);
                    });

            Category newCategory = new Category();
            newCategory.setCategoryDescription(request.getCategoryDescription());
            newCategory.setMasterCategory(linkedMasterCategory);
            newCategory.setMenu(menu); // Assign the menu here
            return categoryService.saveCategory(newCategory); // Save it immediately
        } else {
            // Logic for an EXISTING category remains the same
            if (request.getCategoryId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Existing category ID is required.");
            }
            return categoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));
        }
    }

    // (findOrCreateMenuForBusiness method remains the same)
    // (getOrCreateMasterItem method remains the same)

    /**
     * Maps the validated request data to a new Item entity.
     * This method no longer handles category saving.
     */
    private Item mapRequestToNewItem(ItemCreationRequest request, Category category, MasterItem masterItem, Menu menu) {
        Item newItem = new Item();
        newItem.setItemName(request.getItemName());
        newItem.setItemDescription(request.getItemDescription());
        newItem.setItemDiscount(request.getItemDiscount());
        newItem.setItemImage(request.getItemImage());
        newItem.setVegOrNonVeg(request.getVegOrNonVeg());
        newItem.setItemAvailability(request.getItemAvailability() != null ? request.getItemAvailability() : true);
        newItem.setBestseller(request.getBestseller() != null ? request.getBestseller() : false);
        newItem.setCategory(category);
        newItem.setMasterItem(masterItem);
        newItem.setMenu(menu);
// ADD THIS: Map variants from the request to the new Item entity
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ItemVariant> variants = request.getVariants().stream()
                    .map(variantDto -> {
                        ItemVariant variant = new ItemVariant();
                        variant.setVariantName(variantDto.getVariantName());
                        variant.setPrice(variantDto.getPrice());
                        variant.setItem(newItem); // Link the variant back to the new item
                        return variant;
                    })
                    .collect(Collectors.toList());
            newItem.setVariants(variants);
        }
        return newItem;
    }
    // (validateItemCreationRequest method remains the same)

    /**
     * Extracts and consolidates validation logic for the item creation request.
     */
    public void validateItemCreationRequest(ItemCreationRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request body cannot be null.");
        }
        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item name cannot be empty.");
        }

        // CORRECTED: Validate the list of variants instead of a single price
        if (request.getVariants() == null || request.getVariants().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item must have at least one price variant.");
        }
        for (ItemVariantDto variant : request.getVariants()) {
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

        // Validation check for conflicting flags (remains the same)
        if (request.getIsNewItem() != null && request.getIsNewItem() && request.getMasterItemId() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot create a new item and provide a master item ID at the same time.");
        }
    }

    /**
     * Determines if a new category needs to be created or an existing one used.
     * This method handles all category-related logic.
     */
    private Category getOrCreateCategory(ItemCreationRequest request, Business business) {
        if (request.getIsNewCategory() != null && request.getIsNewCategory()) {
            if (request.getCategoryDescription() == null || request.getCategoryDescription().trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New category requires a description.");
            }

            MasterCategory linkedMasterCategory = masterCategoryService
                    .findByCategoryDescription(request.getCategoryDescription())
                    .orElseGet(() -> {
                        MasterCategory mc = new MasterCategory();
                        mc.setCategoryDescription(request.getCategoryDescription());
                        mc.setBusinessType(business.getBusinessType());
                        return masterCategoryService.save(mc);
                    });

            Category newCategory = new Category();
            newCategory.setCategoryDescription(request.getCategoryDescription());
            newCategory.setMasterCategory(linkedMasterCategory);
            return newCategory;
        } else {
            if (request.getCategoryId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Existing category ID is required.");
            }
            return categoryService.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + request.getCategoryId()));
        }
    }

    /**
     * Finds or creates a menu for the given business.
     */
    private Menu findOrCreateMenuForBusiness(Long businessId, Business business) {
        return menuService.findByBusinessId(businessId)
                .orElseGet(() -> {
                    log.info("No existing menu found for business ID: {}. Creating a default menu.", businessId);
                    Menu newMenu = new Menu();
                    newMenu.setMenuName("Default Menu for " + business.getBusinessName());
                    newMenu.setBusiness(business);
                    newMenu.setCreatedDate(LocalDateTime.now());
                    newMenu.setUpdatedDate(LocalDateTime.now());
                    return menuService.save(newMenu);
                });
    }

    /**
     * Creates and saves a new MasterItem or fetches an existing one.
     */
    private MasterItem getOrCreateMasterItem(ItemCreationRequest request,Long masterId,Category category) {
        if (request.getIsNewItem() != null && request.getIsNewItem()) {
            log.info("isNewItem is true, creating a new MasterItem.");
            MasterItem newMasterItem = new MasterItem();
            newMasterItem.setItemName(request.getItemName());
            newMasterItem.setItemDescription(request.getItemDescription());
            newMasterItem.setItemImage(request.getItemImage());
            newMasterItem.setMasterCategory(category.getMasterCategory());

            return masterItemService.createMasterItem(newMasterItem);
        } else if (request.getMasterItemId() != null) {
            log.info("isNewItem is false, fetching existing MasterItem with ID: {}", request.getMasterItemId());
            return masterItemService.getMasterItemById(request.getMasterItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Master Item not found with ID: " + request.getMasterItemId()));
        }
        return null; // No MasterItem specified or created
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
        existingItem.setItemDiscount(request.getItemDiscount());
        existingItem.setItemImage(request.getItemImage());
        existingItem.setVegOrNonVeg(request.getVegOrNonVeg());

        // Handle null Booleans from DTO for primitive boolean fields
        existingItem.setItemAvailability(request.getItemAvailability() != null ? request.getItemAvailability() : false);
        existingItem.setBestseller(request.getBestseller() != null ? request.getBestseller() : false);

        // Set the resolved Category entity
        existingItem.setCategory(newCategory);
        // Clear all existing variants to avoid stale data
        existingItem.getVariants().clear();
        itemVariantRepository.deleteByItemId(itemId); // You'll need to create this method

        // Create and add new variants from the request
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<ItemVariant> newVariants = request.getVariants().stream()
                    .map(variantDto -> {
                        ItemVariant variant = new ItemVariant();
                        variant.setVariantName(variantDto.getVariantName());
                        variant.setPrice(variantDto.getPrice());
                        variant.setItem(existingItem);
                        return variant;
                    })
                    .collect(Collectors.toList());
            existingItem.getVariants().addAll(newVariants);
        }

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
    // New method to handle bulk item creation
    @Transactional
    public List<Item> createBulkItemsForBusiness(Long businessId, List<ItemCreationRequest> requests) {
        List<Item> createdItems = new ArrayList<>();
        for (ItemCreationRequest request : requests) {
            Item createdItem = createItemForBusiness(businessId, request);
            createdItems.add(createdItem);
        }
        return createdItems;
    }
}