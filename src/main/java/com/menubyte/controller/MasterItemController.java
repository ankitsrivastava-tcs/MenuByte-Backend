package com.menubyte.controller;

import com.menubyte.entity.MasterItem;
import com.menubyte.service.MasterItemService;
import org.springframework.http.HttpStatus; // Import HttpStatus for potential future use or explicit error responses
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/master-items")
public class MasterItemController {

    private final MasterItemService masterItemService;

    public MasterItemController(MasterItemService masterItemService) {
        this.masterItemService = masterItemService;
    }

    /**
     * Creates a new Master Item.
     * @param masterItem The item to be created.
     * @return The created MasterItem object.
     */
    @PostMapping
    public ResponseEntity<MasterItem> createMasterItem(@RequestBody MasterItem masterItem) {
        MasterItem createdItem = masterItemService.createMasterItem(masterItem);
        return ResponseEntity.ok(createdItem);
    }

    /**
     * Retrieves a Master Item by its ID.
     * @param id The ID of the master item.
     * @return The corresponding MasterItem object.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MasterItem> getMasterItemById(@PathVariable Long id) { // Renamed for clarity to match service
        MasterItem masterItem = masterItemService.getMasterItemById(id)
                .orElseThrow(() -> new RuntimeException("MasterItem not found with id " + id));
        return ResponseEntity.ok(masterItem);
    }

    /**
     * Retrieves a Master Item by its name (case-insensitive).
     * @param itemName The name of the master item to search for.
     * @return The corresponding MasterItem object.
     */
    @GetMapping("/by-name/{itemName}") // Changed to path variable for cleaner URL, or keep as @RequestParam if preferred
    public ResponseEntity<MasterItem> getMasterItemByName(@PathVariable String itemName) { // Renamed for clarity to match service
        MasterItem masterItem = masterItemService.getMasterItemByNameIgnoreCase(itemName) // Using IgnoreCase method
                .orElseThrow(() -> new RuntimeException("MasterItem not found with name " + itemName));
        return ResponseEntity.ok(masterItem);
    }

    /**
     * Retrieves all Master Items belonging to a specific Master Category ID.
     * @param masterCategoryId The ID of the master category.
     * @return A list of MasterItem objects.
     */
    @GetMapping("/by-master-category/{masterCategoryId}")
    public ResponseEntity<List<MasterItem>> getMasterItemsByMasterCategory(@PathVariable Long masterCategoryId) { // Renamed for clarity to match service
        List<MasterItem> masterItems = masterItemService.getMasterItemsByMasterCategoryId(masterCategoryId); // Using correct service method
        return ResponseEntity.ok(masterItems);
    }

    /**
     * Updates an existing Master Item.
     * @param id The ID of the master item to update.
     * @param updatedItem The updated MasterItem object.
     * @return The updated MasterItem object.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MasterItem> updateMasterItem(@PathVariable Long id, @RequestBody MasterItem updatedItem) {
        MasterItem item = masterItemService.updateMasterItem(id, updatedItem);
        return ResponseEntity.ok(item);
    }

    /**
     * Deletes a Master Item.
     * @param id The ID of the master item to delete.
     * @return A success message confirming deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMasterItem(@PathVariable Long id) { // Changed return type to Void for 204 No Content
        masterItemService.deleteMasterItem(id);
        return ResponseEntity.noContent().build(); // Use noContent().build() for 204 success
    }

    /**
     * Retrieves all Master Items.
     * @return A list of all MasterItem objects.
     */
    @GetMapping
    public ResponseEntity<List<MasterItem>> getAllMasterItems() {
        List<MasterItem> items = masterItemService.getAllMasterItems();
        return ResponseEntity.ok(items);
    }
}
