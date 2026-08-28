package com.phegondev.InventoryManagementSystem.branch;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<Response> getAll() {
        List<BranchDTO> branches = branchService.getAll();
        return ResponseEntity.ok(Response.builder()
                .status(200).message("Branches retrieved")
                .branches(branches).build());
    }

    @GetMapping("/active")
    public ResponseEntity<Response> getActive() {
        List<BranchDTO> branches = branchService.getActive();
        return ResponseEntity.ok(Response.builder()
                .status(200).message("Active branches")
                .branches(branches).build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER','SUPER_ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<Response> getById(@PathVariable Long id) {
        BranchDTO dto = branchService.getById(id);
        return ResponseEntity.ok(Response.builder()
                .status(200).message("Branch")
                .branch(dto).build());
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> create(@Valid @RequestBody BranchDTO dto) {
        BranchDTO created = branchService.create(dto);
        return ResponseEntity.ok(Response.builder()
                .status(200).message("Branch created")
                .branch(created).build());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> update(@PathVariable Long id, @RequestBody BranchDTO dto) {
        BranchDTO updated = branchService.update(id, dto);
        return ResponseEntity.ok(Response.builder()
                .status(200).message("Branch updated")
                .branch(updated).build());
    }

    @PutMapping("/disable/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> disable(@PathVariable Long id) {
        branchService.disable(id);
        return ResponseEntity.ok(Response.builder().status(200).message("Branch disabled").build());
    }

    @PutMapping("/archive/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> archive(@PathVariable Long id) {
        branchService.archive(id);
        return ResponseEntity.ok(Response.builder().status(200).message("Branch archived").build());
    }

    @PutMapping("/{branchId}/manager/{managerId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> assignManager(@PathVariable Long branchId, @PathVariable Long managerId) {
        BranchDTO dto = branchService.assignManager(branchId, managerId);
        return ResponseEntity.ok(Response.builder()
                .status(200).message("Manager assigned")
                .branch(dto).build());
    }

    @DeleteMapping("/{branchId}/manager")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> removeManager(@PathVariable Long branchId) {
        BranchDTO dto = branchService.removeManager(branchId);
        return ResponseEntity.ok(Response.builder()
                .status(200).message("Manager removed")
                .branch(dto).build());
    }
}
