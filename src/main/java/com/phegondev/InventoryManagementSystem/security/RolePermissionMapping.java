package com.phegondev.InventoryManagementSystem.security;

import com.phegondev.InventoryManagementSystem.enums.Permission;
import com.phegondev.InventoryManagementSystem.enums.UserRole;

import java.util.EnumSet;
import java.util.Set;

public final class RolePermissionMapping {
    private RolePermissionMapping() {}

    public static Set<Permission> forRole(UserRole role) {
        if (role == null) return Set.of();
        return switch (role) {
            case SUPER_ADMIN, ADMIN -> EnumSet.allOf(Permission.class);
            case BRANCH_MANAGER, MANAGER -> EnumSet.of(
                    Permission.POS_VIEW, Permission.POS_CREATE,
                    Permission.PRODUCT_VIEW, Permission.INVENTORY_VIEW, Permission.INVENTORY_ADJUST,
                    Permission.SALE_VIEW, Permission.SALE_CREATE,
                    Permission.PURCHASE_VIEW, Permission.PURCHASE_CREATE,
                    Permission.INVOICE_VIEW, Permission.INVOICE_CREATE,
                    Permission.CUSTOMER_VIEW, Permission.CUSTOMER_MANAGE,
                    Permission.SUPPLIER_VIEW, Permission.SUPPLIER_MANAGE,
                    Permission.EMPLOYEE_VIEW, Permission.EMPLOYEE_CREATE, Permission.EMPLOYEE_UPDATE, Permission.EMPLOYEE_DELETE,
                    Permission.REPORT_VIEW, Permission.REPORT_FINANCIAL_VIEW,
                    Permission.WAREHOUSE_MANAGE, Permission.AUDIT_VIEW,
                    Permission.STOCK_TRANSFER_CREATE, Permission.STOCK_TRANSFER_APPROVE
            );
            case PHARMACIST -> EnumSet.of(
                    Permission.POS_VIEW, Permission.POS_CREATE,
                    Permission.PRODUCT_VIEW, Permission.INVENTORY_VIEW,
                    Permission.SALE_VIEW, Permission.INVOICE_VIEW,
                    Permission.CUSTOMER_VIEW
            );
            case CASHIER -> EnumSet.of(
                    Permission.POS_CREATE, Permission.POS_VIEW,
                    Permission.PRODUCT_VIEW, Permission.INVOICE_CREATE, Permission.INVOICE_VIEW,
                    Permission.CUSTOMER_VIEW
            );
            case INVENTORY_STAFF -> EnumSet.of(
                    Permission.INVENTORY_VIEW, Permission.INVENTORY_ADJUST,
                    Permission.PRODUCT_VIEW, Permission.STOCK_TRANSFER_CREATE,
                    Permission.REPORT_VIEW
            );
            case ACCOUNTANT -> EnumSet.of(
                    Permission.REPORT_VIEW, Permission.REPORT_FINANCIAL_VIEW,
                    Permission.INVOICE_VIEW, Permission.SALE_VIEW, Permission.PURCHASE_VIEW,
                    Permission.AUDIT_VIEW
            );
        };
    }

    public static boolean has(UserRole role, Permission permission) {
        return forRole(role).contains(permission);
    }
}
