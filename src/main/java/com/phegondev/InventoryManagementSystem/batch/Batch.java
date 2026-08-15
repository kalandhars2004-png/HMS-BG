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
@Table(name = "batches")
public class Batch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
