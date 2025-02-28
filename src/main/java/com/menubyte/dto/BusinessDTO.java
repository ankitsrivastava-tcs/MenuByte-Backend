package com.menubyte.dto;

import com.menubyte.enums.BusinessType;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessDTO {
    private Long id;
    private String businessName;
    private String businessLogo;
    private String tagline;
    private BusinessType businessType;
}

