package com.phegondev.InventoryManagementSystem.kitting;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kits")
@RequiredArgsConstructor
public class KitController {

    private final KitService kitService;

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> addKit(@RequestBody KitDTO kitDTO) {
        return ResponseEntity.ok(kitService.addKit(kitDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllKits() {
        return ResponseEntity.ok(kitService.getAllKits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getKitById(@PathVariable Long id) {
        return ResponseEntity.ok(kitService.getKitById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateKit(@PathVariable Long id, @RequestBody KitDTO kitDTO) {
        return ResponseEntity.ok(kitService.updateKit(id, kitDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteKit(@PathVariable Long id) {
        return ResponseEntity.ok(kitService.deleteKit(id));
    }

    @PostMapping("/{id}/assemble")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> assembleKit(@PathVariable Long id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(kitService.assembleKit(id, quantity));
    }

    @PostMapping("/{id}/disassemble")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> disassembleKit(@PathVariable Long id, @RequestParam Integer quantity) {
        return ResponseEntity.ok(kitService.disassembleKit(id, quantity));
    }
}
