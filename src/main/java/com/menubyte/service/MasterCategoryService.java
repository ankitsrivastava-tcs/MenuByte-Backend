/**
 * Service for managing Master Category entities.
 * Handles category creation, retrieval, updates, and deletion.
 *
 * @author Ankit Srivastava
 */
package com.menubyte.service;

import com.menubyte.entity.MasterCategory;
import com.menubyte.repository.MasterCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MasterCategoryService {

    private final MasterCategoryRepository masterCategoryRepository;

    public MasterCategoryService(MasterCategoryRepository masterCategoryRepository) {
        this.masterCategoryRepository = masterCategoryRepository;
    }

    /**
     * Create a Master Category.
     * @param masterCategory MasterCategory object.
     * @return Created MasterCategory object.
     */
    public MasterCategory createMasterCategory(MasterCategory masterCategory) {
        log.info("Creating master category: {}", masterCategory);
        return masterCategoryRepository.save(masterCategory);
    }

    /**
     * Get a Master Category by ID.
     * @param id MasterCategory ID.
     * @return MasterCategory object.
     */
    public MasterCategory getMasterCategoryById(Long id) {
        log.info("Fetching master category with ID: {}", id);
        return masterCategoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Master Category not found with ID: {}", id);
                    return new RuntimeException("Master Category not found");
                });
    }

    /**
     * Update a Master Category.
     * @param id MasterCategory ID.
     * @param updatedCategory Updated category details.
     * @return Updated MasterCategory object.
     */
    public MasterCategory updateMasterCategory(Long id, MasterCategory updatedCategory) {
        log.info("Updating master category with ID: {}", id);
        MasterCategory existingCategory = getMasterCategoryById(id);
        existingCategory.setCategoryDescription(updatedCategory.getCategoryDescription());
        MasterCategory updated = masterCategoryRepository.save(existingCategory);
        log.info("Master category updated successfully: {}", updated);
        return updated;
    }

    /**
     * Delete a Master Category.
     * @param id MasterCategory ID.
     */
    public void deleteMasterCategory(Long id) {
        log.info("Deleting master category with ID: {}", id);
        masterCategoryRepository.deleteById(id);
    }

    /**
     * Get all Master Categories.
     * @return List of MasterCategory objects.
     */
    public List<MasterCategory> getAllMasterCategories() {
        log.info("Fetching all master categories");
        return masterCategoryRepository.findAll();
    }

    public MasterCategory save(MasterCategory masterCategory) {
        return masterCategoryRepository.save(masterCategory);
    }
    public Optional<MasterCategory> findByCategoryDescription(String categoryDescription) {
        return masterCategoryRepository.findByCategoryDescription(categoryDescription);
    }
}
