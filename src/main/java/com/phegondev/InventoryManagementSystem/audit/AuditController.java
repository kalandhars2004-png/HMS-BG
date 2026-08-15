package com.phegondev.InventoryManagementSystem.audit;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/all")
    public ResponseEntity<Response> getAllLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {
        List<AuditLogDTO> logs;
        if (entityType != null && entityId != null) {
            logs = auditService.getLogsForEntity(entityType, entityId);
        } else {
            logs = auditService.getAllLogs();
        }
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Audit logs retrieved")
                .auditLogs(logs)
                .build());
    }

    @GetMapping("/type/{entityType}")
    public ResponseEntity<Response> getLogsByEntityType(@PathVariable String entityType) {
        List<AuditLogDTO> logs = auditService.getLogsByEntityType(entityType);
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Audit logs retrieved for " + entityType)
                .auditLogs(logs)
                .build());
    }

    @DeleteMapping("/clean")
    // hasRole('ADMIN') expands to the authority ROLE_ADMIN, but AuthUser grants the
    // bare enum name. This was the only hasRole in the codebase and it denied everyone.
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> cleanOldLogs(@RequestParam(defaultValue = "90") int days) {
        auditService.clearOldLogs(days);
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Audit logs older than " + days + " days deleted")
                .build());
    }
}
