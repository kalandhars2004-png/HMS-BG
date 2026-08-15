package com.phegondev.InventoryManagementSystem.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogDTO {
    private Long id;
    private String entityType;
    private Long entityId;
    private String action;
    private String description;
    private String changedBy;
    private String oldValue;
    private String newValue;
    private LocalDateTime createdAt;
}
