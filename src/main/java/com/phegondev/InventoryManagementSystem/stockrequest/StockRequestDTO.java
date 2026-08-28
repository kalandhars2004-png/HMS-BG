package com.phegondev.InventoryManagementSystem.stockrequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockRequestDTO {
    private Long id;
    private Long organizationId;
    private Long branchId;
    private Long sourceBranchId;
    private Long sourceWarehouseId;
    private Long productId;
    private String productName;
    private Integer quantity;
    private String reason;
    private StockRequestStatus status;
    private Long requestedBy;
    private String requestedByName;
    private Long approvedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
}
