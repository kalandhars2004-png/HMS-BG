package com.phegondev.InventoryManagementSystem.stocktransfer;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-transfers")
@RequiredArgsConstructor
public class StockTransferController {

    private final StockTransferService stockTransferService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createStockTransfer(@RequestBody @Valid StockTransferDTO stockTransferDTO) {
        return ResponseEntity.ok(stockTransferService.createStockTransfer(stockTransferDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllStockTransfers() {
        return ResponseEntity.ok(stockTransferService.getAllStockTransfers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getStockTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(stockTransferService.getStockTransferById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateStockTransfer(@PathVariable Long id, @RequestBody @Valid StockTransferDTO stockTransferDTO) {
        return ResponseEntity.ok(stockTransferService.updateStockTransfer(id, stockTransferDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteStockTransfer(@PathVariable Long id) {
        return ResponseEntity.ok(stockTransferService.deleteStockTransfer(id));
    }
}

