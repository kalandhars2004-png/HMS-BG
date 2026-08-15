package com.phegondev.InventoryManagementSystem.stockcount;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stock-counts")
@RequiredArgsConstructor
public class StockCountController {

    private final StockCountService stockCountService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> createStockCount(@RequestBody @Valid StockCountDTO stockCountDTO) {
        return ResponseEntity.ok(stockCountService.createStockCount(stockCountDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllStockCounts() {
        return ResponseEntity.ok(stockCountService.getAllStockCounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getStockCountById(@PathVariable Long id) {
        return ResponseEntity.ok(stockCountService.getStockCountById(id));
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateStockCountStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(stockCountService.updateStockCountStatus(id, body.get("status")));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addItem(@PathVariable Long id, @RequestBody @Valid StockCountItemDTO itemDTO) {
        return ResponseEntity.ok(stockCountService.addItemToStockCount(id, itemDTO));
    }

    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateItemCount(@PathVariable Long id, @PathVariable Long itemId, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(stockCountService.updateItemCount(id, itemId, body.get("actualQuantity")));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> completeStockCount(@PathVariable Long id) {
        return ResponseEntity.ok(stockCountService.completeStockCount(id));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteStockCount(@PathVariable Long id) {
        return ResponseEntity.ok(stockCountService.deleteStockCount(id));
    }
}
