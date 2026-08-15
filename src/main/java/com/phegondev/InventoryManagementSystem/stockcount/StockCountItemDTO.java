package com.phegondev.InventoryManagementSystem.stockcount;

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
public class StockCountItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Integer expectedQuantity;
    private Integer actualQuantity;
    private Integer variance;
    private String status;
    private String notes;
}
