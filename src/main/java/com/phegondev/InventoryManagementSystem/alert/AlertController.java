package com.phegondev.InventoryManagementSystem.alert;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

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

    @PutMapping("/read/{id}")
    public ResponseEntity<Response> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(alertService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Response> markAllAsRead() {
        return ResponseEntity.ok(alertService.markAllAsRead());
    }

    @PostMapping("/check")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> checkAlerts() {
        return ResponseEntity.ok(alertService.checkAndCreateAlerts());
    }
}