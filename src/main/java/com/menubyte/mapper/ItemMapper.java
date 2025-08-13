package com.menubyte.mapper;
import com.menubyte.dto.ItemDTO;
import com.menubyte.entity.Item;
import com.menubyte.enums.VegNonVeg; // Ensure this import is correct

/**
 * Mapper class for converting between Item entity and ItemDTO.
 */
public class ItemMapper {

    /**
     * Converts an Item entity to an ItemDTO.
     *
     * @param item The Item entity to convert.
     * @return The corresponding ItemDTO.
     */
    public static ItemDTO toDto(Item item) {
        if (item == null) {
            return null;
        }
        ItemDTO itemDto = new ItemDTO();
        itemDto.setId(item.getId());
        itemDto.setItemName(item.getItemName());
        itemDto.setPrice(item.getItemPrice()); // Mapping itemPrice from entity to price in DTO
        itemDto.setItemDiscount(item.getItemDiscount());
        itemDto.setVegOrNonVeg(item.getVegOrNonVeg());
        itemDto.setBestseller(item.isBestseller());
        itemDto.setItemDescription(item.getItemDescription());
        // itemImage and itemAvailability are in Item entity but not in ItemDTO, so they are not mapped here.
        return itemDto;
    }

    /**
     * Converts an ItemDTO to an Item entity.
     * This method is typically used for creating new Item entities or
     * for updating an existing Item entity with DTO data.
     * Note: Relationships (Category, Menu) are not set by this mapper
     * as they require fetching existing entities from the database.
     *
     * @param itemDto The ItemDTO to convert.
     * @return The corresponding Item entity.
     */
    public static Item toEntity(ItemDTO itemDto) {
        if (itemDto == null) {
            return null;
        }
        Item item = new Item();
        item.setId(itemDto.getId()); // ID might be null for new items, or present for updates
        item.setItemName(itemDto.getItemName());
        item.setItemPrice(itemDto.getPrice()); // Mapping price from DTO to itemPrice in entity
        item.setItemDiscount(itemDto.getItemDiscount());
        item.setVegOrNonVeg(itemDto.getVegOrNonVeg());
        item.setBestseller(itemDto.isBestseller());
        item.setItemDescription(itemDto.getItemDescription());
        // itemImage and itemAvailability are in Item entity but not in ItemDTO, so they are not mapped here.
        // itemAvailability needs to be explicitly set if it's not in DTO but required in entity
        // item.setItemAvailability(itemDto.isItemAvailability()); // Assuming ItemDTO has this field if needed

        return item;
    }

    /**
     * Updates an existing Item entity with data from an ItemDTO.
     * This is useful for partial updates where you don't want to create a new entity.
     *
     * @param itemDto The ItemDTO containing updated data.
     * @param existingItem The existing Item entity to update.
     */
    public static void updateEntityFromDto(ItemDTO itemDto, Item existingItem) {
        if (itemDto == null || existingItem == null) {
            return;
        }

        // Only update fields that are present in the DTO and should be changeable
        if (itemDto.getItemName() != null) {
            existingItem.setItemName(itemDto.getItemName());
        }
        existingItem.setItemPrice(itemDto.getPrice()); // Price is a primitive, always update
        existingItem.setItemDiscount(itemDto.getItemDiscount()); // Discount is a primitive, always update
        if (itemDto.getVegOrNonVeg() != null) {
            existingItem.setVegOrNonVeg(itemDto.getVegOrNonVeg());
        }
        existingItem.setBestseller(itemDto.isBestseller()); // Bestseller is boolean, always update
        if (itemDto.getItemDescription() != null) {
            existingItem.setItemDescription(itemDto.getItemDescription());
        }
            existingItem.setItemAvailability(itemDto.isItemAvailability());
        existingItem.setDealOfTheDay(itemDto.isDealOfTheDay());
    }
}
