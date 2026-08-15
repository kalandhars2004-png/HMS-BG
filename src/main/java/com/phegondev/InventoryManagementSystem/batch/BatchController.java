package com.phegondev.InventoryManagementSystem.batch;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchService batchService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> createBatch(@RequestBody @Valid BatchDTO batchDTO) {
        return ResponseEntity.ok(batchService.createBatch(batchDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getBatchById(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.getBatchById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateBatch(@PathVariable Long id, @RequestBody @Valid BatchDTO batchDTO) {
        return ResponseEntity.ok(batchService.updateBatch(id, batchDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteBatch(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.deleteBatch(id));
    }

    @GetMapping("/expiring-before")
    public ResponseEntity<Response> getBatchesExpiringBefore(@RequestParam("date") LocalDateTime date) {
        return ResponseEntity.ok(batchService.getBatchesExpiringBefore(date));
    }
}
