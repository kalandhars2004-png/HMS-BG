package com.phegondev.InventoryManagementSystem.forecast;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForecastResult {
    private Long productId;
    private String productName;
    private Integer currentStock;
    private Integer averageMonthlySales;
    private Integer daysUntilOutOfStock;
    private String recommendation;
    private Integer suggestedOrderQuantity;
    private LocalDateTime calculatedAt;
}
