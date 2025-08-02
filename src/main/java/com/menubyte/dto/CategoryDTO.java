package com.menubyte.dto;

import com.menubyte.entity.Category;
import com.menubyte.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long id;
    private String categoryName;
    private Long masterCategoryId; // <-- NEW FIELD

    private List<ItemDTO> items;

    public CategoryDTO(Category category, List<Item> items) {
        this.id = category.getId();
        this.categoryName = category.getCategoryDescription();
        this.masterCategoryId = category.getMasterCategory() != null ? category.getMasterCategory().getId() : null; // <-- Populate masterCategoryId
        this.items = items.stream()
                .map(ItemDTO::new)
                .collect(Collectors.toList());
    }
}