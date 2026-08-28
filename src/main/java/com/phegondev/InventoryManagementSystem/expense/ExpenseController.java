package com.phegondev.InventoryManagementSystem.expense;

import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseRepository repo;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN','ADMIN','BRANCH_MANAGER','MANAGER','ACCOUNTANT')")
    public ResponseEntity<Expense> create(@RequestBody Expense e) {
        var t = TenantContext.get();
        if (t == null) throw new org.springframework.security.access.AccessDeniedException("No tenant");
        Long bid = e.getBranchId() != null ? e.getBranchId() : t.branchId();
        if (bid == null) bid = 1L;
        if (!t.isSuperAdmin() && !bid.equals(t.branchId())) throw new org.springframework.security.access.AccessDeniedException("Branch mismatch");
        e.setBranchId(bid);
        e.setOrganizationId(t.organizationId() != null ? t.organizationId() : 1L);
        e.setCreatedBy(t.userId());
        return ResponseEntity.ok(repo.save(e));
    }

    @GetMapping
    public ResponseEntity<List<Expense>> list() {
        var t = TenantContext.get();
        if (t == null) throw new org.springframework.security.access.AccessDeniedException("No tenant");
        if (t.isSuperAdmin() && t.branchId() == null) {
            return ResponseEntity.ok(repo.findByOrganizationIdOrderByDateDesc(t.organizationId() != null ? t.organizationId() : 1L));
        }
        Long bid = t.branchId() != null ? t.branchId() : 1L;
        return ResponseEntity.ok(repo.findByBranchIdOrderByDateDesc(bid));
    }
}
