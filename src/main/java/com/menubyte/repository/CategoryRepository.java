package com.menubyte.repository;

import com.menubyte.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds a Category by its ID and the ID of the Menu it belongs to.
     */
    Optional<Category> findByMenuIdAndId(Long menuId, Long categoryId);

    /**
     * Finds all Categories belonging to a specific Menu ID.
     */
    List<Category> findByMenuId(Long menuId);

    /**
     * Finds a Category by its description and the ID of the Menu it belongs to.
     * This is useful due to the unique constraint on (category_description, menu_id).
     */
    Optional<Category> findByCategoryDescriptionAndMenuId(String categoryDescription, Long menuId);

    /**
     * Finds all Categories associated with a specific MasterCategory ID.
     */
    List<Category> findByMasterCategoryId(Long masterCategoryId);
}
