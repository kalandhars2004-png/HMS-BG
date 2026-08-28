package com.phegondev.InventoryManagementSystem.tenant;

import com.phegondev.InventoryManagementSystem.enums.UserRole;

/**
 * ThreadLocal branch tenancy — set by TenantFilter for every authenticated request.
 * SUPER_ADMIN bypass: branchId may be null & isSuperAdmin true.
 */
public final class TenantContext {
    private static final ThreadLocal<Tenant> TL = new ThreadLocal<>();

    private TenantContext() {}

    public record Tenant(Long userId, String email, UserRole role, Long branchId, Long organizationId, boolean isSuperAdmin) {}

    public static void set(Tenant tenant) { TL.set(tenant); }
    public static Tenant get() { return TL.get(); }
    public static void clear() { TL.remove(); }

    public static boolean isSuperAdmin() {
        Tenant t = get();
        return t != null && t.isSuperAdmin();
    }

    public static Long requireBranchId() {
        Tenant t = get();
        if (t == null) throw new IllegalStateException("No tenant context");
        if (t.isSuperAdmin()) {
            // Super admin may operate with explicit branch via header; if none, 0 means "all"
            return t.branchId();
        }
        if (t.branchId() == null) throw new IllegalStateException("Branch context missing for non-super-admin");
        return t.branchId();
    }

    public static Long getBranchIdOrNull() {
        Tenant t = get();
        return t == null ? null : t.branchId();
    }
}
