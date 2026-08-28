package com.phegondev.InventoryManagementSystem.stockmovement;

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
@Table(name = "stock_movements", indexes = {
    @Index(name = "idx_sm_product_id", columnList = "product_id"),
    @Index(name = "idx_sm_movement_type", columnList = "movement_type"),
    @Index(name = "idx_sm_created_at", columnList = "created_at")
})
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "organization_id")
    private Long organizationId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_sku", nullable = false)
    private String productSku;

    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "movement_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MovementType movementType;

    @Column(name = "quantity_in")
    private Integer quantityIn;

    @Column(name = "quantity_out")
    private Integer quantityOut;

    @Column(name = "balance_stock", nullable = false)
    private Integer balanceStock;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
