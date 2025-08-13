package com.menubyte.dto;

import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MenuDTO {
    private Long id;
    private String businessName;
    private List<CategoryDTO> categories;
    @Enumerated(EnumType.STRING)
    private com.menubyte.enums.BusinessType businessType;
    @Enumerated(EnumType.STRING)
    private com.menubyte.enums.SubscriptionStatus subscriptionStatus;
    @Enumerated(EnumType.STRING)
    private com.menubyte.enums.UserType userType;

    public MenuDTO(Menu menu) {
        this.id = menu.getId();
        this.businessName = menu.getBusiness().getBusinessName();

        // Group items by category, then sort the categories and their items
        this.categories = menu.getItems().stream()
                .collect(Collectors.groupingBy(Item::getCategory))
                .entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getId())) // Sort categories by ID
                .map(entry -> {
                    // Sort items within each category by ID
                    List<Item> sortedItems = entry.getValue().stream()
                            .sorted(Comparator.comparing(Item::getId))
                            .collect(Collectors.toList());
                    return new CategoryDTO(entry.getKey(), sortedItems);
                })
                .collect(Collectors.toList());

        this.businessType = menu.getBusiness().getBusinessType();
        // Assuming subscriptionStatus and userType are also part of the Menu entity
        // this.subscriptionStatus = menu.getSubscriptionStatus();
        // this.userType = menu.getUserType();
    }
}