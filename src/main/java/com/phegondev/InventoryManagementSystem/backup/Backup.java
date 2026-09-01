package com.phegondev.InventoryManagementSystem.backup;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Records a completed backup run. The backup file holds the data of every branch
 * (or a single selected branch) exported together so cross-branch relationships are
 * preserved. Superadmin only.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "backups", indexes = {
        @Index(name = "idx_backups_created", columnList = "created_at"),
        @Index(name = "idx_backups_scope", columnList = "branch_id")
})
public class Backup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** null = all branches; otherwise the single branch that was backed up. */
    @Column(name = "branch_id")
    private Long branchId;

    /** File name stored on disk, e.g. backup-2026-09-01-0300.json.gz */
    @Column(name = "file_name", nullable = false)
    private String fileName;

    /** Human label shown in history, e.g. "All branches" or branch name. */
    @Column(name = "label")
    private String label;

    /** COMPLETED | FAILED */
    @Column(nullable = false)
    private String status;

    /** Seed status while the dump is still running (async). */
    @Column(name = "progress_pct")
    private Integer progressPct;

    private Long sizeBytes;

    private String detail;

    /** Public Drive web link after upload; null when Drive is not configured. */
    @Column(name = "drive_url")
    private String driveUrl;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = "RUNNING";
        if (progressPct == null) progressPct = 0;
    }
}
