package com.phegondev.InventoryManagementSystem.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BackupDTO {
    private Long id;
    private Long branchId;
    private String fileName;
    private String label;
    private String status;
    private Integer progressPct;
    private Long sizeBytes;
    private String detail;
    private String driveUrl;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
