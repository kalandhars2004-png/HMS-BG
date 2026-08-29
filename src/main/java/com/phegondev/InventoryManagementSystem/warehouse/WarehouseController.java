package com.phegondev.InventoryManagementSystem.warehouse;

import com.phegondev.InventoryManagementSystem.warehouse.WarehouseDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.warehouse.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;


    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createWarehouse(@RequestBody @Valid WarehouseDTO warehouseDTO) {
        return ResponseEntity.ok(warehouseService.createWarehouse(warehouseDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Response> getWarehouseById(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateWarehouse(@PathVariable Long id, @RequestBody @Valid WarehouseDTO warehouseDTO) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, warehouseDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteWarehouse(@PathVariable Long id) {
        return ResponseEntity.ok(warehouseService.deleteWarehouse(id));
    }


}

