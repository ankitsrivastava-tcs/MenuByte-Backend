/**
 * Service for managing Master Item entities.
 * Handles item creation, retrieval, updates, and deletion.
 *
 * @author Ankit Srivastava
 */
package com.menubyte.service;

import com.menubyte.entity.MasterItem;
import com.menubyte.repository.MasterItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MasterItemService {

    private final MasterItemRepository masterItemRepository;

    public MasterItemService(MasterItemRepository masterItemRepository) {
        this.masterItemRepository = masterItemRepository;
    }

    /**
     * Create a Master Item.
     * @param masterItem MasterItem object.
     * @return Created MasterItem object.
     */
    public MasterItem createMasterItem(MasterItem masterItem) {
        log.info("Creating master item: {}", masterItem);
        return masterItemRepository.save(masterItem);
    }

    /**
     * Get a Master Item by ID.
     * @param id MasterItem ID.
     * @return MasterItem object.
     */
    public MasterItem getMasterItemById(Long id) {
        log.info("Fetching master item with ID: {}", id);
        return masterItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Master Item not found with ID: {}", id);
                    return new RuntimeException("Master Item not found");
                });
    }

    /**
     * Update a Master Item.
     * @param id MasterItem ID.
     * @param updatedItem Updated item details.
     * @return Updated MasterItem object.
     */
    public MasterItem updateMasterItem(Long id, MasterItem updatedItem) {
        log.info("Updating master item with ID: {}", id);
        MasterItem existingItem = getMasterItemById(id);
        existingItem.setItemDescription(updatedItem.getItemDescription());
        existingItem.setItemPrice(updatedItem.getItemPrice());
        existingItem.setItemImage(updatedItem.getItemImage());
        existingItem.setCategory(updatedItem.getCategory());
        MasterItem updated = masterItemRepository.save(existingItem);
        log.info("Master item updated successfully: {}", updated);
        return updated;
    }

    /**
     * Delete a Master Item.
     * @param id MasterItem ID.
     */
    public void deleteMasterItem(Long id) {
        log.info("Deleting master item with ID: {}", id);
        masterItemRepository.deleteById(id);
    }

    /**
     * Get all Master Items.
     * @return List of MasterItem objects.
     */
    public List<MasterItem> getAllMasterItems() {
        log.info("Fetching all master items");
        return masterItemRepository.findAll();
    }
}