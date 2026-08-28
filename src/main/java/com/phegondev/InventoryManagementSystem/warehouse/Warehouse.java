package com.phegondev.InventoryManagementSystem.warehouse;

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
@Table(name = "warehouses", indexes = {
        @Index(name = "idx_warehouses_branch", columnList = "branch_id"),
        @Index(name = "idx_warehouses_status", columnList = "status")
})
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "branch_id")
    private Long branchId;

    private String warehouse;
    @Column(name = "contact_person")
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    @Column(name = "postal_code")
    private String postalCode;
    @Column(name = "total_products")
    private Integer totalProducts;
    private Integer stock;
    private Integer qty;
    private Boolean status;
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (createdOn == null) createdOn = LocalDateTime.now();
        if (status == null) status = true;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
