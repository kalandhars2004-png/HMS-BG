package com.phegondev.InventoryManagementSystem.rma;

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
@Table(name = "return_requests")
public class ReturnRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String returnNumber;

    private Long salesOrderId;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private String reason;

    private String type;

    private String status;

    @Column(name = "item_condition")
    private String condition;

    private BigDecimal refundAmount;

    private String notes;

    private LocalDateTime requestDate;

    private LocalDateTime receivedDate;

    private LocalDateTime completedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
