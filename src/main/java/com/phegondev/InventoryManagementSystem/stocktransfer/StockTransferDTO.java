package com.phegondev.InventoryManagementSystem.stocktransfer;

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
public class StockTransferDTO {
    private Long id;
    private Long productId;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Integer quantity;
    private String description;
    private String status;
    private String fromWarehouseName;
    private String toWarehouseName;
    private String productName;
    private LocalDateTime createdAt;
}
