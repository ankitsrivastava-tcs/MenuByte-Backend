/**
 * Controller for managing Master Categories.
 * Provides endpoints for creating, retrieving, updating, and deleting master categories.
 *
 * @author Ankit
 */
package com.menubyte.controller;

import com.menubyte.entity.MasterCategory;
import com.menubyte.enums.BusinessType;
import com.menubyte.service.MasterCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for handling Master Category operations.
 */
@RestController
@RequestMapping("/api/master-categories")
public class MasterCategoryController {

    private final MasterCategoryService masterCategoryService;

    /**
     * Constructor for MasterCategoryController.
     *
     * @param masterCategoryService Service for managing master categories.
     */
    public MasterCategoryController(MasterCategoryService masterCategoryService) {
        this.masterCategoryService = masterCategoryService;
    }

    /**
     * Creates a new Master Category.
     *
     * @param masterCategory The MasterCategory object to be created.
     * @return The created MasterCategory.
     */
    @PostMapping
    public ResponseEntity<MasterCategory> createMasterCategory(@RequestBody MasterCategory masterCategory) {
        MasterCategory createdCategory = masterCategoryService.createMasterCategory(masterCategory);
        return ResponseEntity.ok(createdCategory);
    }

    /**
     * Retrieves a Master Category by ID.
     *
     * @param id The ID of the MasterCategory.
     * @return The requested MasterCategory.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MasterCategory> getMasterCategory(@PathVariable Long id) {
        MasterCategory category = masterCategoryService.getMasterCategoryById(id);
        return ResponseEntity.ok(category);
    }

    /**
     * Updates an existing Master Category.
     *
     * @param id The ID of the MasterCategory to be updated.
     * @param updatedCategory The updated MasterCategory object.
     * @return The updated MasterCategory.
     */
    @PutMapping("/{id}")
    public ResponseEntity<MasterCategory> updateMasterCategory(@PathVariable Long id, @RequestBody MasterCategory updatedCategory) {
        MasterCategory category = masterCategoryService.updateMasterCategory(id, updatedCategory);
        return ResponseEntity.ok(category);
    }

    /**
     * Deletes a Master Category.
     *
     * @param id The ID of the MasterCategory to be deleted.
     * @return A success message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMasterCategory(@PathVariable Long id) {
        masterCategoryService.deleteMasterCategory(id);
        return ResponseEntity.ok("Master Category deleted successfully.");
    }

    /**
     * Retrieves all Master Categories.
     *
     * @return A list of all MasterCategories.
     */
    @GetMapping
    public ResponseEntity<List<MasterCategory>> getAllMasterCategories(@RequestParam(required = false) BusinessType businessType) {
        // Fetch ALL master categories first
        List<MasterCategory> categories = masterCategoryService.getAllMasterCategories();

        // Apply the filter in the application layer based on the request parameter
        if (businessType != null) {
            // Filter by the provided businessType
            List<MasterCategory> filteredCategories = categories.stream()
                    .filter(k -> k.getBusinessType() != null && k.getBusinessType().equals(businessType))
                    .collect(Collectors.toUnmodifiableList());
            return ResponseEntity.ok(filteredCategories);
        } else {
            // If no businessType parameter is provided, return all categories
            return ResponseEntity.ok(categories);
        }
}}