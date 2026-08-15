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
    private Long productId;
    private Long fromWarehouseId;
    private Long toWarehouseId;
    private Integer quantity;
    private String description;
    private String status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
