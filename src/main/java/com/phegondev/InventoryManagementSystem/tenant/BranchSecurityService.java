package com.phegondev.InventoryManagementSystem.tenant;

import com.phegondev.InventoryManagementSystem.branch.BranchRepository;
import com.phegondev.InventoryManagementSystem.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchSecurityService {

    private final BranchRepository branchRepository;

    public boolean canAccess(Long branchId) {
        TenantContext.Tenant t = TenantContext.get();
        if (t == null) return false;
        if (t.isSuperAdmin()) return true;
        // Branch-scoped users can only access their own branch
        if (branchId == null) return false;
        return branchId.equals(t.branchId());
    }

    public boolean canAccessAny() {
        return TenantContext.get() != null;
    }

    public boolean isSuperAdmin() {
        return TenantContext.isSuperAdmin();
    }

    public void enforceAccess(Long branchId) {
        if (!canAccess(branchId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access denied to branch " + branchId);
        }
    }

    public boolean isSuperAdminRole(UserRole role) {
        return role == UserRole.SUPER_ADMIN || role == UserRole.ADMIN;
    }
}
