package com.phegondev.InventoryManagementSystem.inventory;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Branch×Warehouse×Product×Batch stock ledger — the single source of truth for quantity_on_hand.
 * FEFO (§16) picks the earliest expiry row first; POS and transfers operate on this table.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(name = "uk_inventory_branch_warehouse_product_batch", columnNames = {"branch_id", "warehouse_id", "product_id", "batch_id"})
}, indexes = {
        @Index(name = "idx_inv_branch", columnList = "branch_id"),
        @Index(name = "idx_inv_branch_product", columnList = "branch_id,product_id"),
        @Index(name = "idx_inv_branch_warehouse_product", columnList = "branch_id,warehouse_id,product_id"),
        @Index(name = "idx_inv_product_batch", columnList = "product_id,batch_id")
})
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand;

    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (quantityOnHand == null) quantityOnHand = 0;
        if (reservedQuantity == null) reservedQuantity = 0;
        if (organizationId == null) organizationId = 1L;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public int getAvailable() {
        return (quantityOnHand != null ? quantityOnHand : 0) - (reservedQuantity != null ? reservedQuantity : 0);
    }
}
