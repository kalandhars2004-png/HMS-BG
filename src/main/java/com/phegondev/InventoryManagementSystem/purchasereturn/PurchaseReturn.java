package com.phegondev.InventoryManagementSystem.purchasereturn;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "purchase_returns")
public class PurchaseReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private Long productId;
    @NotNull
    private Long supplierId;
    @NotNull
    private Integer quantity;
    @NotBlank
    private String reason;
    @NotNull
    private BigDecimal returnAmount;
    private String status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
