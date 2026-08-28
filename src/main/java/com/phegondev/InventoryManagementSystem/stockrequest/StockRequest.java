package com.phegondev.InventoryManagementSystem.stockrequest;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Stock request §12 — branch requests stock from warehouse or another branch.
 * Workflow: PENDING → APPROVED/REJECTED → PREPARING → SHIPPED → RECEIVED
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "stock_requests", indexes = {
        @Index(name = "idx_sr_branch", columnList = "branch_id"),
        @Index(name = "idx_sr_source_branch", columnList = "source_branch_id"),
        @Index(name = "idx_sr_status", columnList = "status"),
        @Index(name = "idx_sr_product", columnList = "product_id")
})
public class StockRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "source_branch_id")
    private Long sourceBranchId;

    @Column(name = "source_warehouse_id")
    private Long sourceWarehouseId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StockRequestStatus status;

    @Column(name = "requested_by")
    private Long requestedBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "shipped_by")
    private Long shippedBy;

    @Column(name = "received_by")
    private Long receivedBy;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
        if (status == null) status = StockRequestStatus.PENDING;
        if (organizationId == null) organizationId = 1L;
    }
}
