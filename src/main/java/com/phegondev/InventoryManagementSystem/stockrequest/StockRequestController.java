package com.phegondev.InventoryManagementSystem.stockrequest;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stock-requests")
@RequiredArgsConstructor
public class StockRequestController {

    private final StockRequestService service;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','BRANCH_MANAGER','MANAGER','INVENTORY_STAFF','PHARMACIST')")
    public ResponseEntity<Response> create(@RequestBody StockRequestDTO dto) {
        StockRequestDTO created = service.create(dto);
        return ResponseEntity.ok(Response.builder().status(200).message("Stock request created").build());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StockRequestDTO>> list() {
        return ResponseEntity.ok(service.getForCurrentBranch());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('SUPER_ADMIN') || hasAuthority('ADMIN')")
    public ResponseEntity<List<StockRequestDTO>> all() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockRequestDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','BRANCH_MANAGER','MANAGER')")
    public ResponseEntity<StockRequestDTO> approve(@PathVariable Long id) {
        return ResponseEntity.ok(service.approve(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','BRANCH_MANAGER','MANAGER')")
    public ResponseEntity<StockRequestDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(service.reject(id));
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<StockRequestDTO> ship(@PathVariable Long id) {
        return ResponseEntity.ok(service.ship(id));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','BRANCH_MANAGER','INVENTORY_STAFF','MANAGER')")
    public ResponseEntity<StockRequestDTO> receive(@PathVariable Long id) {
        return ResponseEntity.ok(service.receive(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<StockRequestDTO> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }
}
