package com.menubyte.repository;

import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Finds an Item by its ID and the ID of the Menu it belongs to.
     */
    Optional<Item> findByMenuIdAndId(Long menuId, Long itemId);

    /**
     * Finds all Items belonging to a specific Menu ID.
     */
    List<Item> findByMenuId(Long menuId);

    /**
     * Finds a list of Items by their IDs and the ID of the Menu they belong to.
     */
    List<Item> findByMenuIdAndIdIn(Long menuId, List<Long> itemIds);

    /**
     * Finds an Item by its ID, the ID of the Category it belongs to, and the ID of the Menu it belongs to.
     */
    Optional<Item> findByCategoryIdAndMenuIdAndId(Long categoryId, Long menuId, Long itemId);

    /**
     * Finds all Items belonging to a specific Category ID.
     */
    List<Item> findByCategoryId(Long categoryId);

    /**
     * Finds all Items belonging to a specific Category ID and Menu ID.
     */
    List<Item> findByCategoryIdAndMenuId(Long categoryId, Long menuId);

    /**
     * Finds all Items that are marked as bestseller for a given Menu ID.
     */
    List<Item> findByMenuIdAndBestsellerTrue(Long menuId);

    /**
     * Finds all Items by their VegNonVeg type for a given Menu ID.
     */

    /**
     * Finds items by name containing a given string (case-insensitive) within a specific menu.
     */
    List<Item> findByMenuIdAndItemNameContainingIgnoreCase(Long menuId, String itemName);
    List<Item> findByMenu(Menu menu);

}
