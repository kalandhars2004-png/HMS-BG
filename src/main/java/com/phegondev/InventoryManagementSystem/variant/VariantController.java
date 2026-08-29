package com.phegondev.InventoryManagementSystem.variant;

import com.phegondev.InventoryManagementSystem.variant.VariantDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.variant.VariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/variants")
@RequiredArgsConstructor
public class VariantController {

    private final VariantService variantService;


    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createVariant(@RequestBody @Valid VariantDTO variantDTO) {
        return ResponseEntity.ok(variantService.createVariant(variantDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllVariants() {
        return ResponseEntity.ok(variantService.getAllVariants());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Response> getVariantById(@PathVariable Long id) {
        return ResponseEntity.ok(variantService.getVariantById(id));
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateVariant(@PathVariable Long id, @RequestBody @Valid VariantDTO variantDTO) {
        return ResponseEntity.ok(variantService.updateVariant(id, variantDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteVariant(@PathVariable Long id) {
        return ResponseEntity.ok(variantService.deleteVariant(id));
    }


}

