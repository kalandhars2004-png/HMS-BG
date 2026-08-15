package com.phegondev.InventoryManagementSystem.rack;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/racks")
@RequiredArgsConstructor
public class RackController {

    private final RackService rackService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> createRack(@RequestBody @Valid RackDTO rackDTO) {
        return ResponseEntity.ok(rackService.createRack(rackDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllRacks() {
        return ResponseEntity.ok(rackService.getAllRacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getRackById(@PathVariable Long id) {
        return ResponseEntity.ok(rackService.getRackById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateRack(@PathVariable Long id, @RequestBody @Valid RackDTO rackDTO) {
        return ResponseEntity.ok(rackService.updateRack(id, rackDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteRack(@PathVariable Long id) {
        return ResponseEntity.ok(rackService.deleteRack(id));
    }
}
