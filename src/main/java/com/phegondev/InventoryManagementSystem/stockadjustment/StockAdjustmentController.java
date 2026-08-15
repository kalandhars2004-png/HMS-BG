package com.phegondev.InventoryManagementSystem.stockadjustment;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-adjustments")
@RequiredArgsConstructor
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> createStockAdjustment(@RequestBody @Valid StockAdjustmentDTO stockAdjustmentDTO) {
        return ResponseEntity.ok(stockAdjustmentService.createStockAdjustment(stockAdjustmentDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllStockAdjustments() {
        return ResponseEntity.ok(stockAdjustmentService.getAllStockAdjustments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getStockAdjustmentById(@PathVariable Long id) {
        return ResponseEntity.ok(stockAdjustmentService.getStockAdjustmentById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateStockAdjustment(@PathVariable Long id, @RequestBody @Valid StockAdjustmentDTO stockAdjustmentDTO) {
        return ResponseEntity.ok(stockAdjustmentService.updateStockAdjustment(id, stockAdjustmentDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteStockAdjustment(@PathVariable Long id) {
        return ResponseEntity.ok(stockAdjustmentService.deleteStockAdjustment(id));
    }
}
