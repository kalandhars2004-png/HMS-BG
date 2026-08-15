package com.phegondev.InventoryManagementSystem.audit;

import java.util.List;

public interface AuditService {
    void log(String entityType, Long entityId, String action, String description, String changedBy, String oldValue, String newValue);
    List<AuditLogDTO> getAllLogs();
    List<AuditLogDTO> getLogsForEntity(String entityType, Long entityId);
    List<AuditLogDTO> getLogsByEntityType(String entityType);
    void clearOldLogs(int days);
}
