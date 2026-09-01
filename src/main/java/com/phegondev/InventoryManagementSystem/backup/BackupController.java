package com.phegondev.InventoryManagementSystem.backup;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;
    private final BackupProperties properties;

    // Superadmin-only: backup spans every branch, a single non-super-admin must not
    // be able to export the whole organisation's data.
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Response> create(@RequestParam(required = false) Long branchId) {
        BackupDTO dto = backupService.createBackup(branchId);
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Backup started")
                .backup(dto)
                .build());
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Response> history() {
        List<BackupDTO> list = backupService.getHistory();
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Backup history")
                .backups(list)
                .build());
    }

    @GetMapping("/latest")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Response> latest() {
        BackupDTO dto = backupService.getLatest();
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Latest backup")
                .backup(dto)
                .build());
    }

    @GetMapping("/status")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<Response> status() {
        boolean drive = properties.driveConfigured();
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message(drive ? "Google Drive configured" : "Cloud storage not configured — backups stored locally")
                .cloudConfigured(drive)
                .build());
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<?> download(@PathVariable Long id) {
        String fileName = backupService.resolveFileName(id);
        if (fileName == null) {
            return ResponseEntity.badRequest().body("Backup not found or not yet completed");
        }
        Path file = Path.of(properties.getDir()).toAbsolutePath().resolve(fileName);
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new FileSystemResource(file.toFile()));
    }
}
