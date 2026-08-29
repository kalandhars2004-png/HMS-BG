package com.phegondev.InventoryManagementSystem.salesorder;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createSalesOrder(@RequestBody @Valid SalesOrderDTO salesOrderDTO) {
        return ResponseEntity.ok(salesOrderService.createSalesOrder(salesOrderDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllSalesOrders() {
        return ResponseEntity.ok(salesOrderService.getAllSalesOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getSalesOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderById(id));
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateSalesOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(salesOrderService.updateSalesOrderStatus(id, body.get("status")));
    }

    @PutMapping("/payment/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updatePaymentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(salesOrderService.updatePaymentStatus(id, body.get("paymentStatus")));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteSalesOrder(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.deleteSalesOrder(id));
    }
}

