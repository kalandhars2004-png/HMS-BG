package com.phegondev.InventoryManagementSystem.purchaseorder;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addPurchaseOrder(@RequestBody PurchaseOrderDTO purchaseOrderDTO) {
        return ResponseEntity.ok(purchaseOrderService.addPurchaseOrder(purchaseOrderDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllPurchaseOrders() {
        return ResponseEntity.ok(purchaseOrderService.getAllPurchaseOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(purchaseOrderService.updateStatus(id, body.get("status")));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> receiveItems(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.receiveItems(id));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deletePurchaseOrder(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.deletePurchaseOrder(id));
    }
}
