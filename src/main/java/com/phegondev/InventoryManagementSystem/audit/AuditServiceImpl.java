package com.phegondev.InventoryManagementSystem.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditWriter auditWriter;

    @Override
    public void log(String entityType, Long entityId, String action, String description, String changedBy, String oldValue, String newValue) {
        // Resolve the actor on the CALLER thread — SecurityContext is thread-local
        // and invisible to the async writer.
        String user = changedBy;
        if (user == null || user.isBlank()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                user = auth.getName();
            }
        }

        AuditLog auditLog = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .description(description)
                .changedBy(user)
                .oldValue(oldValue)
                .newValue(newValue)
                .createdAt(LocalDateTime.now())
                .build();

        auditWriter.write(auditLog);
    }

    @Override
    public List<AuditLogDTO> getAllLogs() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getLogsForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuditLogDTO> getLogsByEntityType(String entityType) {
        return auditLogRepository.findByEntityType(entityType)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void clearOldLogs(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        List<AuditLog> oldLogs = auditLogRepository.findAll().stream()
                .filter(log -> log.getCreatedAt().isBefore(cutoff))
                .collect(Collectors.toList());
        auditLogRepository.deleteAll(oldLogs);
    }

    private AuditLogDTO toDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .description(log.getDescription())
                .changedBy(log.getChangedBy())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
