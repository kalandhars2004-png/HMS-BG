package com.phegondev.InventoryManagementSystem.manufacturing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BOMItemDTO {

    private Long id;

    private Long rawMaterialId;

    private String rawMaterialName;

    private Integer quantity;

    private String unit;
}
