package com.phegondev.InventoryManagementSystem.manufacturing;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manufacturing")
@RequiredArgsConstructor
public class MfrController {

    private final MfrService mfrService;

    @PostMapping("/bom/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> addBOM(@RequestBody BillOfMaterialsDTO billOfMaterialsDTO) {
        return ResponseEntity.ok(mfrService.addBOM(billOfMaterialsDTO));
    }

    @GetMapping("/bom/all")
    public ResponseEntity<Response> getAllBOMs() {
        return ResponseEntity.ok(mfrService.getAllBOMs());
    }

    @GetMapping("/bom/{id}")
    public ResponseEntity<Response> getBOMById(@PathVariable Long id) {
        return ResponseEntity.ok(mfrService.getBOMById(id));
    }

    @PutMapping("/bom/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateBOM(@PathVariable Long id, @RequestBody BillOfMaterialsDTO billOfMaterialsDTO) {
        return ResponseEntity.ok(mfrService.updateBOM(id, billOfMaterialsDTO));
    }

    @DeleteMapping("/bom/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteBOM(@PathVariable Long id) {
        return ResponseEntity.ok(mfrService.deleteBOM(id));
    }

    @PostMapping("/orders/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> addProductionOrder(@RequestBody ProductionOrderDTO productionOrderDTO) {
        return ResponseEntity.ok(mfrService.addProductionOrder(productionOrderDTO));
    }

    @GetMapping("/orders/all")
    public ResponseEntity<Response> getAllProductionOrders() {
        return ResponseEntity.ok(mfrService.getAllProductionOrders());
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<Response> getProductionOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(mfrService.getProductionOrderById(id));
    }

    @PutMapping("/orders/status/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateProductionOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(mfrService.updateProductionOrderStatus(id, status));
    }

    @PostMapping("/orders/{id}/start")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> startProduction(@PathVariable Long id) {
        return ResponseEntity.ok(mfrService.startProduction(id));
    }

    @PostMapping("/orders/{id}/complete")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> completeProduction(@PathVariable Long id) {
        return ResponseEntity.ok(mfrService.completeProduction(id));
    }
}

