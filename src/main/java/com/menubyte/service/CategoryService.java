package com.menubyte.service;

import com.menubyte.entity.Category;
import com.menubyte.entity.MasterCategory;
import com.menubyte.entity.Menu;
import com.menubyte.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j // Enables Lombok's logger for logging
@Service // Marks this class as a Spring Service component
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final MasterCategoryService masterCategoryService; // Dependency for managing MasterCategory entities

    /**
     * Constructor for CategoryService, injecting required dependencies.
     * Spring automatically provides these dependencies (CategoryRepository, MasterCategoryService).
     *
     * @param categoryRepository    The repository for Category entities.
     * @param masterCategoryService The service for MasterCategory entities.
     */
    public CategoryService(CategoryRepository categoryRepository, MasterCategoryService masterCategoryService) {
        this.categoryRepository = categoryRepository;
        this.masterCategoryService = masterCategoryService;
    }

    /**
     * Creates a new category for a specific menu, ensuring it's linked to an appropriate
     * MasterCategory (creating one if it doesn't exist). This method handles the
     * primary business logic for new category creation.
     *
     * @param categoryDescription The descriptive name of the new category (e.g., "Appetizers", "Main Course").
     * @param menu                The Menu entity to which this new category will belong.
     * @return The newly created and persisted Category entity.
     * @throws ResponseStatusException (HttpStatus.CONFLICT) if a category with the same description
     * already exists within the provided menu.
     */
    @Transactional // Ensures the entire method executes within a single transaction
    public Category createCategory(String categoryDescription, Menu menu) {
        log.info("Attempting to create new category: '{}' for Menu ID: {}", categoryDescription, menu.getId());

        // 1. Check for uniqueness: A category with the same description should not exist
        // within the scope of the same menu.
        Optional<Category> existingCategory = categoryRepository.findByCategoryDescriptionAndMenuId(categoryDescription, menu.getId());
        if (existingCategory.isPresent()) {
            log.warn("Category '{}' already exists for Menu ID: {}. Conflict detected.", categoryDescription, menu.getId());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with description '" + categoryDescription + "' already exists for this menu.");
        }

        // 2. Handle MasterCategory association: Every Category must be linked to a MasterCategory.
        // We find an existing MasterCategory by description or create a new one if it doesn't exist.
        MasterCategory linkedMasterCategory = masterCategoryService.findByCategoryDescription(categoryDescription)
                .orElseGet(() -> {
                    log.info("MasterCategory for '{}' not found, creating a new one.", categoryDescription);
                    MasterCategory newMasterCategory = new MasterCategory();
                    newMasterCategory.setCategoryDescription(categoryDescription);
                    return masterCategoryService.save(newMasterCategory); // Persist the new MasterCategory
                });
        log.debug("Category '{}' will be linked to MasterCategory ID: {}", categoryDescription, linkedMasterCategory.getId());

        // 3. Create and persist the new Category entity.
        Category newCategory = new Category();
        newCategory.setCategoryDescription(categoryDescription);
        newCategory.setMenu(menu); // Establish the relationship with the Menu entity
        newCategory.setMasterCategory(linkedMasterCategory); // Establish the relationship with the MasterCategory

        // Use the generic saveCategory method internally to persist the newly constructed category
        Category savedCategory = this.saveCategory(newCategory);
        log.info("New category created successfully with ID: {} for Menu ID: {}", savedCategory.getId(), menu.getId());
        return savedCategory;
    }

    /**
     * Provides a generic save operation for a Category entity.
     * Use this if you have a Category object already constructed and just need to persist it
     * or update its state in the database.
     * For creating a *new* category that follows the MasterCategory and Menu association rules,
     * it's recommended to use the {@code createCategory()} method.
     *
     * @param category The Category entity to be saved or updated.
     * @return The persisted Category entity.
     */
    @Transactional // Ensures the save operation is atomic
    public Category saveCategory(Category category) {
        log.info("Saving category with ID: {} (description: {})", category.getId(), category.getCategoryDescription());
        return categoryRepository.save(category);
    }

    /**
     * Retrieves a list of all existing categories in the system.
     *
     * @return A list of all Category entities.
     */
    public List<Category> getAllCategories() {
        log.debug("Fetching all categories.");
        return categoryRepository.findAll();
    }

    /**
     * Retrieves a single Category entity by its unique ID.
     *
     * @param categoryId The ID of the category to retrieve.
     * @return The found Category entity.
     * @throws ResponseStatusException (HttpStatus.NOT_FOUND) if no category with the given ID is found.
     */
    public Category getCategoryById(Long categoryId) {
        log.debug("Fetching category with ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", categoryId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + categoryId);
                });
    }

    /**
     * Finds a Category entity by its unique ID and returns it wrapped in an Optional.
     * This is useful when the existence of the category is uncertain and a {@code ResponseStatusException}
     * should not be thrown immediately if it's not found.
     *
     * @param categoryId The ID of the category to find.
     * @return An {@code Optional} containing the Category if found, or an empty {@code Optional} otherwise.
     */
    public Optional<Category> findById(Long categoryId) {
        log.debug("Looking up category with ID: {}", categoryId);
        return categoryRepository.findById(categoryId);
    }

    /**
     * Updates an existing Category entity with new details.
     * This method also ensures that the updated category description remains unique
     * within its associated menu.
     *
     * @param categoryId      The ID of the category to be updated.
     * @param updatedCategory A Category object containing the new details (e.g., description).
     * @return The updated and persisted Category entity.
     * @throws ResponseStatusException (HttpStatus.NOT_FOUND) if the category to be updated does not exist.
     * @throws ResponseStatusException (HttpStatus.CONFLICT) if the new description conflicts with an existing
     * category's description within the same menu.
     */
    @Transactional // Ensures the update operation is atomic
    public Category updateCategory(Long categoryId, Category updatedCategory) {
        log.info("Attempting to update category with ID: {}", categoryId);
        // First, retrieve the existing category. getCategoryById will throw if not found.
        Category existingCategory = getCategoryById(categoryId);

        // Check for description change and potential conflicts within the same menu.
        // We ignore conflicts with the category being updated itself.
        if (!existingCategory.getCategoryDescription().equalsIgnoreCase(updatedCategory.getCategoryDescription())) {
            Optional<Category> conflictCategory = categoryRepository.findByCategoryDescriptionAndMenuId(
                    updatedCategory.getCategoryDescription(),
                    existingCategory.getMenu().getId()); // Check uniqueness within the *same* menu as the existing category
            if (conflictCategory.isPresent() && !conflictCategory.get().getId().equals(categoryId)) {
                log.warn("Update conflict: Category '{}' already exists for Menu ID: {}. Cannot update category ID {}.",
                        updatedCategory.getCategoryDescription(), existingCategory.getMenu().getId(), categoryId);
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Category with description '" + updatedCategory.getCategoryDescription() + "' already exists for this menu.");
            }
        }

        // Apply updates to the existing entity
        existingCategory.setCategoryDescription(updatedCategory.getCategoryDescription());
        // Add other fields to update here if needed (e.g., existingCategory.setActive(updatedCategory.isActive());)

        // Use the generic saveCategory method to persist the changes
        Category updated = this.saveCategory(existingCategory);
        log.info("Category with ID: {} updated successfully.", updated.getId());
        return updated;
    }

    /**
     * Deletes a Category entity by its unique ID.
     *
     * @param categoryId The ID of the category to delete.
     * @throws ResponseStatusException (HttpStatus.NOT_FOUND) if the category to be deleted does not exist.
     */
    @Transactional // Ensures the delete operation is atomic
    public void deleteCategory(Long categoryId) {
        log.info("Attempting to delete category with ID: {}", categoryId);
        // Check if the category exists before attempting to delete it.
        if (!categoryRepository.existsById(categoryId)) {
            log.error("Category not found with ID: {} for deletion.", categoryId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found with ID: " + categoryId);
        }
        categoryRepository.deleteById(categoryId); // Perform the deletion
        log.info("Category with ID: {} deleted successfully.", categoryId);
    }
}