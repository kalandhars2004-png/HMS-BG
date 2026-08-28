package com.phegondev.InventoryManagementSystem.stockmovement;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stock-movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping("/all")
    public ResponseEntity<Response> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String searchText) {

        return ResponseEntity.ok(stockMovementService.getAll(page, size, searchText));
    }
}
