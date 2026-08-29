package com.phegondev.InventoryManagementSystem.purchasereturn;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purchase-returns")
@RequiredArgsConstructor
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createPurchaseReturn(@RequestBody @Valid PurchaseReturnDTO purchaseReturnDTO) {
        return ResponseEntity.ok(purchaseReturnService.createPurchaseReturn(purchaseReturnDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllPurchaseReturns() {
        return ResponseEntity.ok(purchaseReturnService.getAllPurchaseReturns());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getPurchaseReturnById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.getPurchaseReturnById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updatePurchaseReturn(@PathVariable Long id, @RequestBody @Valid PurchaseReturnDTO purchaseReturnDTO) {
        return ResponseEntity.ok(purchaseReturnService.updatePurchaseReturn(id, purchaseReturnDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deletePurchaseReturn(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseReturnService.deletePurchaseReturn(id));
    }
}

