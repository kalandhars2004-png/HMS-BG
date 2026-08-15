package com.phegondev.InventoryManagementSystem.stockadjustment;

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
@Table(name = "stock_adjustments")
public class StockAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    private String adjustmentType;
    private Integer quantity;
    private String reason;
    private String referenceNo;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
