package com.phegondev.InventoryManagementSystem.brand;

import com.phegondev.InventoryManagementSystem.brand.BrandDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.brand.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;


    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createBrand(@RequestBody @Valid BrandDTO brandDTO) {
        return ResponseEntity.ok(brandService.createBrand(brandDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Response> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateBrand(@PathVariable Long id, @RequestBody @Valid BrandDTO brandDTO) {
        return ResponseEntity.ok(brandService.updateBrand(id, brandDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteBrand(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.deleteBrand(id));
    }


}

