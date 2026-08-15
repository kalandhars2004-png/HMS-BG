package com.phegondev.InventoryManagementSystem.stockadjustment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockAdjustmentDTO {
    private Long id;
    private Long productId;
    private String adjustmentType;
    private Integer quantity;
    private String reason;
    private String referenceNo;
    private String productName;
    private LocalDateTime createdAt;
}
