package com.menubyte.repository;

import com.menubyte.entity.MasterItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasterItemRepository extends JpaRepository<MasterItem, Long> {

    /**
     * Finds a MasterItem by its ID and the ID of the MasterCategory it belongs to.
     */
    Optional<MasterItem> findByCategoryIdAndId(Long categoryId, Long masterItemId);

    /**
     * Finds all MasterItems belonging to a specific MasterCategory ID.
     */
    List<MasterItem> findByCategoryId(Long categoryId);

    /**
     * Finds MasterItems by item description containing a given string (case-insensitive).
     */
    List<MasterItem> findByItemDescriptionContainingIgnoreCase(String itemDescription);
}
