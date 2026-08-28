package com.phegondev.InventoryManagementSystem.batch;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "batches", indexes = {
        // Expiry range scans (near-expiry/expired counts and alerts) and the
        // per-product FEFO batch lookup.
        @Index(name = "idx_batches_expiry_date", columnList = "expiry_date"),
        @Index(name = "idx_batches_product_id", columnList = "product_id")
})
public class Batch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "organization_id")
    private Long organizationId;
    private Long productId;
    @NotBlank
    private String batchNo;
    private Integer quantity;
    private BigDecimal mrp;
    private BigDecimal purchasePrice;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expiryDate;
    private Boolean status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
