package com.phegondev.InventoryManagementSystem.pos;

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
@Table(name = "pos_transactions", indexes = {
        // EOD pulls completed bills for a date window; sessions list their bills.
        @Index(name = "idx_pos_tx_status_created", columnList = "status,created_at"),
        @Index(name = "idx_pos_tx_session_id", columnList = "session_id")
})
public class POSTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "organization_id")
    private Long organizationId;

    private Long sessionId;

    private String receiptNumber;

    private Long productId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private String paymentMethod;

    private String status;

    private Long customerId;

    private String customerName;

    @Column(name = "biller_id")
    private Long billerId;

    @Column(name = "biller_name")
    private String billerName;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
