package com.menubyte.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSellingItemDTO {
    private Long itemId;
    private String itemName;
    private Long totalSold;
}