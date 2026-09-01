package com.phegondev.InventoryManagementSystem.backup;

import java.util.List;

public interface BackupService {
    BackupDTO createBackup(Long branchId);
    List<BackupDTO> getHistory();
    BackupDTO getLatest();
    String resolveFileName(Long backupId);
}
