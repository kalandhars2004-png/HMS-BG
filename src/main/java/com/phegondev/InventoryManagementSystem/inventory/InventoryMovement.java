package com.phegondev.InventoryManagementSystem.inventory;

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
@Table(name = "inventory_movements")
public class InventoryMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String movementType;
    private String referenceType;
    private String referenceNumber;
    private Long productId;
    private String productName;
    private Long batchId;
    private String batchNo;
    private Long warehouseId;
    private String warehouseName;
    private Integer quantity;
    private Integer previousStock;
    private Integer currentStock;
    private String invoiceNumber;
    private String notes;
    private String performedBy;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}