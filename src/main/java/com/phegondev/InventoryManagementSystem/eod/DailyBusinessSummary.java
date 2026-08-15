package com.phegondev.InventoryManagementSystem.eod;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "daily_business_summary")
public class DailyBusinessSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate reportDate;

    // Sales
    private BigDecimal totalSales;
    private Integer totalBills;
    private Integer totalItemsSold;

    // Revenue & Profit
    private BigDecimal totalRevenue;
    private BigDecimal grossProfit;
    private BigDecimal netProfit;

    // Financial breakdown
    private BigDecimal cashSales;
    private BigDecimal upiSales;
    private BigDecimal cardSales;
    private BigDecimal walletSales;
    private BigDecimal totalDiscount;
    private BigDecimal totalGst;
    private BigDecimal totalRefunds;
    private BigDecimal totalExpenses;

    // Purchases
    private BigDecimal totalPurchases;
    private Integer purchaseOrdersCount;

    // Customers
    private Integer totalCustomers;
    private Integer newCustomers;
    private BigDecimal averageBillValue;
    private BigDecimal highestBill;
    private BigDecimal lowestBill;
    private Integer loyaltyPointsEarned;

    // Inventory
    private BigDecimal openingStockValue;
    private BigDecimal closingStockValue;
    private Integer lowStockCount;
    private Integer outOfStockCount;
    private Integer nearExpiryCount;
    private Integer expiredCount;

    // Payments
    private BigDecimal cashDrawerOpening;
    private BigDecimal cashDrawerClosing;
    private BigDecimal expectedCash;

    // Employee
    private String bestEmployee;
    private Integer bestEmployeeSales;

    // Top items
    @Column(length = 500)
    private String topMedicine;
    @Column(length = 500)
    private String topCategory;
    @Column(length = 500)
    private String topCustomer;

    // Metadata
    private String generatedBy;
    private LocalDateTime generatedAt;

    @PrePersist
    protected void onCreate() { generatedAt = LocalDateTime.now(); }
}