package com.phegondev.InventoryManagementSystem.customer;

import jakarta.persistence.*;
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
@Table(name = "customers", indexes = {
        // Phone is the POS lookup key; created_at powers the daily new-customer count.
        @Index(name = "idx_customers_phone", columnList = "phone"),
        @Index(name = "idx_customers_created_at", columnList = "created_at")
})
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "organization_id")
    private Long organizationId;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Integer loyaltyPoints;
    private BigDecimal lifetimeSpend;
    private Integer purchaseCount;
    private LocalDateTime lastPurchase;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (loyaltyPoints == null) loyaltyPoints = 0;
        if (lifetimeSpend == null) lifetimeSpend = BigDecimal.ZERO;
        if (purchaseCount == null) purchaseCount = 0;
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}