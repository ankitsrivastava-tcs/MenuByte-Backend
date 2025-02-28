/**
 * Controller for managing Master Items.
 * Handles operations such as creation, retrieval, updating, and deletion of master items.
 *
 * @author Ankit
 */
package com.menubyte.controller;

import com.menubyte.entity.MasterItem;
import com.menubyte.service.MasterItemService;
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
    public ResponseEntity<MasterItem> getMasterItem(@PathVariable Long id) {
        MasterItem item = masterItemService.getMasterItemById(id);
        return ResponseEntity.ok(item);
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
    public ResponseEntity<String> deleteMasterItem(@PathVariable Long id) {
        masterItemService.deleteMasterItem(id);
        return ResponseEntity.ok("Master Item deleted successfully.");
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