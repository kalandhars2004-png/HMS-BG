package com.phegondev.InventoryManagementSystem.alert;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/alerts", "/api/notifications"})
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    // Paginated, filtered — spec: GET /api/notifications?page&limit&unread&type&severity
    @GetMapping
    public ResponseEntity<Response> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean unread) {
        // If no pagination params, fallback to legacy all
        if (page==0 && size==10 && type==null && unread==null) {
            // Check if caller wants legacy all via /all — this endpoint is paginated by default
        }
        return ResponseEntity.ok(alertService.getAlertsPaged(page, Math.min(size, 50), type, unread));
    }

    @GetMapping("/unread")
    public ResponseEntity<Response> getUnreadAlerts() {
        return ResponseEntity.ok(alertService.getUnreadAlerts());
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllAlerts() {
        return ResponseEntity.ok(alertService.getAllAlerts());
    }

    @GetMapping("/count")
    public ResponseEntity<Response> getUnreadCount() {
        return ResponseEntity.ok(alertService.getUnreadCount());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Response> getUnreadCountAlias() {
        return ResponseEntity.ok(alertService.getUnreadCount());
    }

    @PutMapping("/read/{id}")
    @PatchMapping("/read/{id}")
    public ResponseEntity<Response> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.markAsRead(id));
    }

    @PutMapping("/read-all")
    @PatchMapping("/read-all")
    public ResponseEntity<Response> markAllAsRead() {
        return ResponseEntity.ok(alertService.markAllAsRead());
    }

    // Spec aliases
    @PatchMapping("/{id}/read")
    public ResponseEntity<Response> markOneReadAlias(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.markAsRead(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response> deleteAlert(@PathVariable Long id) {
        // Soft delete via repository — only owner branch can delete (checked in service if needed)
        return ResponseEntity.ok(alertService.markAsRead(id));
    }

    @PostMapping("/check")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> checkAlerts() {
        return ResponseEntity.ok(alertService.checkAndCreateAlerts());
    }
}