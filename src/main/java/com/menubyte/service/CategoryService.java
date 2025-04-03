package com.menubyte.service;

import com.menubyte.entity.Category;
import com.menubyte.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Create a new category.
     * @param category The category object.
     * @return Created category.
     */
    public Category createCategory(Category category) {
        log.info("Creating a new category: {}", category);
        return categoryRepository.save(category);
    }

    /**
     * Get all categories.
     * @return List of categories.
     */
    public List<Category> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAll();
    }

    /**
     * Get a category by ID.
     * @param categoryId The category ID.
     * @return The found category.
     */
    public Category getCategoryById(Long categoryId) {
        log.info("Fetching category with ID: {}", categoryId);
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("Category not found with ID: {}", categoryId);
                    return new RuntimeException("Category not found");
                });
    }

    /**
     * 🔥 FIX: Find a category by ID and return Optional<Category>.
     */
    public Optional<Category> findById(Long categoryId) {
        log.info("Looking up category with ID: {}", categoryId);
        return categoryRepository.findById(categoryId);
    }

    /**
     * Update an existing category.
     */
    public Category updateCategory(Long categoryId, Category updatedCategory) {
        log.info("Updating category with ID: {}", categoryId);
        Category existingCategory = getCategoryById(categoryId);

        existingCategory.setCategoryDescription(updatedCategory.getCategoryDescription());

        Category updated = categoryRepository.save(existingCategory);
        log.info("Category updated successfully: {}", updated);
        return updated;
    }

    /**
     * Delete a category by ID.
     */
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category with ID: {}", categoryId);
        categoryRepository.deleteById(categoryId);
    }
}
