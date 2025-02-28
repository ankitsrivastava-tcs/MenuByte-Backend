package com.menubyte.dto;

import com.menubyte.entity.Category;
import com.menubyte.entity.Item;

import java.util.List;
import java.util.stream.Collectors;

import com.menubyte.entity.Item;
import lombok.Data;

@Data
public class CategoryDTO {
    private String categoryName;
    private List<ItemDTO> items;

    public CategoryDTO(Category category, List<Item> items) {
        this.categoryName = category.getCategoryDescription();
        this.items = items.stream().map(ItemDTO::new).collect(Collectors.toList());
    }
}
