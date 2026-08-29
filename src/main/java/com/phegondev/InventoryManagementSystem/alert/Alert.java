package com.phegondev.InventoryManagementSystem.alert;

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
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alerts_branch", columnList = "branch_id"),
        @Index(name = "idx_alerts_user_read", columnList = "user_id,read_flag"),
        @Index(name = "idx_alerts_created", columnList = "created_at"),
        @Index(name = "idx_alerts_type", columnList = "type")
})
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Type: LOW_STOCK, OUT_OF_STOCK, EXPIRING_SOON, EXPIRED, SALE_CREATED, PURCHASE_CREATED, PAYMENT_SUCCESS, USER_CREATED etc.
    private String type;
    // Severity: INFO, SUCCESS, WARNING, ERROR, CRITICAL
    private String severity;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String message;
    private Long relatedEntityId;
    private String relatedEntityType;
    // Branch/user scoping — reuse existing tenant conventions
    @Column(name = "branch_id")
    private Long branchId;
    @Column(name = "organization_id")
    private Long organizationId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "read_flag")
    private Boolean read;
    private LocalDateTime readAt;
    private Boolean isResolved;
    private LocalDateTime resolvedAt;
    // JSON metadata for medicineId, stock, expiry, payment etc.
    @Column(columnDefinition = "TEXT")
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (read == null) read = false;
        if (isResolved == null) isResolved = false;
    }
    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}