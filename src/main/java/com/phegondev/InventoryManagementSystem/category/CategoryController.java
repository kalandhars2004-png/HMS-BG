package com.phegondev.InventoryManagementSystem.category;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateCategory(@PathVariable Long id, @RequestBody @Valid CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> bulkDelete(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(categoryService.bulkDelete(ids));
    }

    @PostMapping("/bulk-status")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> bulkStatusUpdate(@RequestBody BulkStatusRequest request) {
        return ResponseEntity.ok(categoryService.bulkStatusUpdate(request.getIds(), request.isActive()));
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchCategories(@RequestParam("q") String query) {
        return ResponseEntity.ok(categoryService.searchCategories(query));
    }
}

