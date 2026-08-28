package com.phegondev.InventoryManagementSystem.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Persists audit entries off the request thread. The entity (including the
 * resolved actor) is fully built by the caller BEFORE hand-off — SecurityContext
 * is not readable on executor threads, and business transactions should not wait
 * on an insert they cannot inspect.
 *
 * Trade-off: a hard crash between response and flush can lose the last few
 * entries; failures are logged at ERROR rather than rolled back into the caller's
 * transaction, which would couple unrelated business writes to audit success.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditWriter {

    private final AuditLogRepository auditLogRepository;

    @Async("auditExecutor")
    public void write(AuditLog auditLog) {
        try {
            // enrich with tenant if missing
            try {
                var tenant = com.phegondev.InventoryManagementSystem.tenant.TenantContext.get();
                if (tenant != null) {
                    if (auditLog.getBranchId() == null) auditLog.setBranchId(tenant.branchId());
                    if (auditLog.getUserId() == null) auditLog.setUserId(tenant.userId());
                }
            } catch (Exception ignored) {}
            if (auditLog.getCreatedAt() == null) auditLog.setCreatedAt(java.time.LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } catch (Exception ex) {
            log.error("Failed to persist audit log for {}#{} ({})",
                    auditLog.getEntityType(), auditLog.getEntityId(), auditLog.getAction(), ex);
        }
    }

    public void write(String entityType, String entityId, String action, String description) {
        try {
            Long eid = null;
            try { eid = Long.valueOf(entityId); } catch (Exception ignored) { eid = 0L; }
            String actor = "system";
            Long branchId = null;
            Long userId = null;
            try {
                var tenant = com.phegondev.InventoryManagementSystem.tenant.TenantContext.get();
                if (tenant != null) {
                    actor = tenant.email();
                    branchId = tenant.branchId();
                    userId = tenant.userId();
                } else {
                    var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    if (auth != null) actor = String.valueOf(auth.getName());
                }
            } catch (Exception ignored) {}
            AuditLog logEntry = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(eid)
                    .action(action)
                    .description(description)
                    .changedBy(actor)
                    .branchId(branchId)
                    .userId(userId)
                    .createdAt(java.time.LocalDateTime.now())
                    .build();
            write(logEntry);
        } catch (Exception ex) {
            log.error("Failed to build audit log {}/{} {}", entityType, entityId, action, ex);
        }
    }
}
