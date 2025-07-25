package com.menubyte.service;

import com.menubyte.entity.MasterItem;
import com.menubyte.repository.MasterItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MasterItemService {

    private final MasterItemRepository masterItemRepository;

    public MasterItemService(MasterItemRepository masterItemRepository) {
        this.masterItemRepository = masterItemRepository;
    }

    public List<MasterItem> getAllMasterItems() {
        return masterItemRepository.findAll();
    }

    public Optional<MasterItem> getMasterItemById(Long id) {
        return masterItemRepository.findById(id);
    }

    public MasterItem createMasterItem(MasterItem masterItem) {
        // You might want to add business logic here, e.g., check for duplicate item names
        return masterItemRepository.save(masterItem);
    }

    public MasterItem updateMasterItem(Long id, MasterItem updatedMasterItem) {
        return masterItemRepository.findById(id)
                .map(existingItem -> {
                    existingItem.setItemName(updatedMasterItem.getItemName());
                    existingItem.setItemDescription(updatedMasterItem.getItemDescription());
                    existingItem.setItemPrice(updatedMasterItem.getItemPrice());
                    existingItem.setItemImage(updatedMasterItem.getItemImage());
                    existingItem.setMasterCategory(updatedMasterItem.getMasterCategory()); // Update category if needed
                    return masterItemRepository.save(existingItem);
                })
                .orElseThrow(() -> new RuntimeException("MasterItem not found with id " + id));
    }

    public void deleteMasterItem(Long id) {
        masterItemRepository.deleteById(id);
    }

    /**
     * Finds master items by the ID of their associated master category.
     *
     * @param masterCategoryId The ID of the master category.
     * @return A list of master items belonging to the specified master category.
     */
    public List<MasterItem> getMasterItemsByMasterCategoryId(Long masterCategoryId) {
        // Corrected method call: using findByMasterCategory_Id as defined in the repository
        return masterItemRepository.findByMasterCategory_Id(masterCategoryId);
    }

    /**
     * Finds a master item by its name, ignoring case.
     *
     * @param itemName The name of the item to search for.
     * @return An Optional containing the found MasterItem, or empty if not found.
     */
    public Optional<MasterItem> getMasterItemByNameIgnoreCase(String itemName) {
        return masterItemRepository.findByItemNameIgnoreCase(itemName);
    }
}
