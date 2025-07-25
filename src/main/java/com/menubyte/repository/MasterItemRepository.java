package com.menubyte.repository;

import com.menubyte.entity.MasterItem;
import com.menubyte.entity.MasterCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MasterItemRepository extends JpaRepository<MasterItem, Long> {
    // This method finds MasterItems by their associated MasterCategory object
    List<MasterItem> findByMasterCategory(MasterCategory masterCategory);

    // This method finds a MasterItem by its item name (assuming it's unique or you want the first one)
    Optional<MasterItem> findByItemName(String itemName);

    /**
     * Finds a list of MasterItems by the ID of their associated MasterCategory.
     * This is the correct method to use when querying by the foreign key ID.
     *
     * @param masterCategoryId The ID of the MasterCategory.
     * @return A List of MasterItem entities associated with the given MasterCategory ID.
     */
    List<MasterItem> findByMasterCategory_Id(Long masterCategoryId); // <--- THIS IS THE CORRECT METHOD

    /**
     * Finds a MasterItem by its item name, ignoring case.
     *
     * @param itemName The name of the item to search for.
     * @return An Optional containing the found MasterItem, or empty if not found.
     */
    Optional<MasterItem> findByItemNameIgnoreCase(String itemName); // <--- Added this method

    // IMPORTANT: Ensure you have removed any method like:
    // List<MasterItem> findByCategoryId(Long categoryId);
    // as this method signature is incorrect for the MasterItem entity structure.
}
