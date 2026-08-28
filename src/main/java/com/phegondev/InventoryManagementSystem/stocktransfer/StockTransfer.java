package com.phegondev.InventoryManagementSystem.stocktransfer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "stock_transfers")
public class StockTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "organization_id")
    private Long organizationId;
    private Long productId;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    @Column(name = "from_branch_id")
    private Long fromBranchId;
    @Column(name = "to_branch_id")
    private Long toBranchId;
    private Integer quantity;
    private String description;
    private String status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
