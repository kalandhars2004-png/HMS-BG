package com.phegondev.InventoryManagementSystem.rma;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/returns")
@RequiredArgsConstructor
public class ReturnController {

    private final ReturnService returnService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> createReturn(@RequestBody ReturnRequestDTO returnRequestDTO) {
        return ResponseEntity.ok(returnService.createReturn(returnRequestDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllReturns() {
        return ResponseEntity.ok(returnService.getAllReturns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getReturnById(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.getReturnById(id));
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(returnService.updateStatus(id, body.get("status")));
    }

    @PutMapping("/{returnId}/items/{itemId}/disposition")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateDisposition(
            @PathVariable Long returnId,
            @PathVariable Long itemId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(returnService.updateDisposition(returnId, itemId, body.get("disposition")));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteReturn(@PathVariable Long id) {
        return ResponseEntity.ok(returnService.deleteReturn(id));
    }
}
