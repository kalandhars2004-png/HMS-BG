package com.phegondev.InventoryManagementSystem.equipment;

import com.phegondev.InventoryManagementSystem.equipment.EquipmentDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.equipment.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;


    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createEquipment(@RequestBody @Valid EquipmentDTO equipmentDTO) {
        return ResponseEntity.ok(equipmentService.createEquipment(equipmentDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllEquipments() {
        return ResponseEntity.ok(equipmentService.getAllEquipments());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Response> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateEquipment(@PathVariable Long id, @RequestBody @Valid EquipmentDTO equipmentDTO) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, equipmentDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteEquipment(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.deleteEquipment(id));
    }


}

