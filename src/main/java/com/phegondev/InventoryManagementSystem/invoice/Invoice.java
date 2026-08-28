package com.phegondev.InventoryManagementSystem.invoice;

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
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "organization_id")
    private Long organizationId;

    private String invoiceNumber;

    private Long salesOrderId;

    private Long transactionId;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    private String customerAddress;

    private BigDecimal subtotal;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private String status;

    private String paymentMethod;

    private String notes;

    private LocalDateTime invoiceDate;

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;

    private Long createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
