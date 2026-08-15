package com.phegondev.InventoryManagementSystem.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReorderPointDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private Integer reorderPoint;
    private Integer reorderQuantity;
    private String status;
    private boolean needsReorder;
}
