package com.phegondev.InventoryManagementSystem.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private Long id;
    private Long branchId;
    private Long organizationId;
    private String branchName;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Integer loyaltyPoints;
    private BigDecimal lifetimeSpend;
    private Integer purchaseCount;
    private LocalDateTime lastPurchase;
    private LocalDateTime createdAt;
}
