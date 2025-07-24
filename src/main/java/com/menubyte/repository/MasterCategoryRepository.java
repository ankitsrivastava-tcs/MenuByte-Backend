package com.menubyte.repository;

import com.menubyte.entity.MasterCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MasterCategoryRepository extends JpaRepository<MasterCategory, Long> {

    /**
     * Finds a MasterCategory by its unique category description.
     */
    Optional<MasterCategory> findByCategoryDescription(String categoryDescription);
}
