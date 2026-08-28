package com.phegondev.InventoryManagementSystem.branch;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Enterprise branch — the primary tenant boundary.
 * Warehouses are branches with type CENTRAL_WAREHOUSE / WAREHOUSE (see BranchType).
 * Unlimited branches per organization.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "branches", indexes = {
        @Index(name = "idx_branches_code", columnList = "code", unique = true),
        @Index(name = "idx_branches_status", columnList = "status"),
        @Index(name = "idx_branches_org", columnList = "organization_id")
})
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BranchType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BranchStatus status;

    private String address;
    private String city;
    private String state;
    private String country;
    @Column(name = "postal_code")
    private String postalCode;
    private String phone;
    private String email;
    @Column(name = "tax_number")
    private String taxNumber;
    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = BranchStatus.ACTIVE;
        if (type == null) type = BranchType.RETAIL;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
