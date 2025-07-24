package com.menubyte.dto;

import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MenuDTO {
    private Long id;
    private String businessName;
    private List<CategoryDTO> categories;

    public MenuDTO(Menu menu) {
        this.id = menu.getId();
        this.businessName = menu.getBusiness().getBusinessName();
        this.categories = menu.getItems().stream()
                .collect(Collectors.groupingBy(Item::getCategory))
                .entrySet().stream()
                .map(entry -> new CategoryDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
