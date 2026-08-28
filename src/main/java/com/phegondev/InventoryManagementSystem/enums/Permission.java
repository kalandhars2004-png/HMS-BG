package com.phegondev.InventoryManagementSystem.enums;

/**
 * Fine-grained permissions §5, §27.
 * Roles map to sets; backend checks both role and permission.
 */
public enum Permission {
    // POS
    POS_CREATE,
    POS_VIEW,
    // Products / Inventory
    PRODUCT_VIEW,
    PRODUCT_CREATE,
    PRODUCT_UPDATE,
    PRODUCT_DELETE,
    INVENTORY_VIEW,
    INVENTORY_ADJUST,
    STOCK_TRANSFER_CREATE,
    STOCK_TRANSFER_APPROVE,
    // Sales / Purchases
    SALE_VIEW,
    SALE_CREATE,
    PURCHASE_VIEW,
    PURCHASE_CREATE,
    INVOICE_VIEW,
    INVOICE_CREATE,
    // Masters
    CUSTOMER_VIEW,
    CUSTOMER_MANAGE,
    SUPPLIER_VIEW,
    SUPPLIER_MANAGE,
    // Employees
    EMPLOYEE_VIEW,
    EMPLOYEE_CREATE,
    EMPLOYEE_UPDATE,
    EMPLOYEE_DELETE,
    // Reports
    REPORT_VIEW,
    REPORT_FINANCIAL_VIEW,
    // Admin
    BRANCH_MANAGE,
    WAREHOUSE_MANAGE,
    SETTINGS_MANAGE,
    AUDIT_VIEW,
    USER_ROLE_ASSIGN
}
