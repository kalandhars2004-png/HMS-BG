package com.phegondev.InventoryManagementSystem.branch;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BranchDTO {
    private Long id;
    private Long organizationId;
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private BranchType type;
    private BranchStatus status;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private String taxNumber;
    private String operatingHours;
    private Long managerId;
    private String managerName;
    private String contactPerson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // denormalized stats (computed, not persisted)
    private Long employeeCount;
    private Long warehouseCount;
}
