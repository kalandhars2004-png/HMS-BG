package com.phegondev.InventoryManagementSystem.purchaseorder;

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
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_number")
    private String poNumber;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    private String status;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "expected_delivery")
    private LocalDateTime expectedDelivery;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;
}
