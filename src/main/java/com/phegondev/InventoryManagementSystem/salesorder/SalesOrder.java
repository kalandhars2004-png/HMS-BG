package com.phegondev.InventoryManagementSystem.salesorder;

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
@Table(name = "sales_orders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String soNumber;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    private String customerAddress;

    private BigDecimal subtotal;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private String status;

    private String paymentStatus;

    private String paymentMethod;

    private String notes;

    private LocalDateTime orderDate;

    private LocalDateTime createdAt;

    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
