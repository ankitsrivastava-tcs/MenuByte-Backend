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
    private Long id; // This field already exists

    private String categoryName;
    private List<ItemDTO> items;

    public CategoryDTO(Category category, List<Item> items) {
        this.id = category.getId(); // <--- Added this line to set the ID
        this.categoryName = category.getCategoryDescription();
        this.items = items.stream()
                .map(ItemDTO::new)
                .collect(Collectors.toList());
    }
}
