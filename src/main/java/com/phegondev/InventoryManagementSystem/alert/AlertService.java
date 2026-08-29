package com.phegondev.InventoryManagementSystem.alert;

import com.phegondev.InventoryManagementSystem.alert.Alert;
import com.phegondev.InventoryManagementSystem.batch.Batch;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.product.Product;

public interface AlertService {
    Response checkAndCreateAlerts();
    Response getUnreadAlerts();
    Response getAllAlerts();
    Response markAsRead(Long id);
    Response markAllAsRead();
    Response getUnreadCount();
    Response getAlertsPaged(int page, int size, String type, Boolean unreadOnly);
    Response deleteAlert(Long id);
    // Centralized helper for event-driven creation
    com.phegondev.InventoryManagementSystem.alert.Alert createNotification(String type, String severity, String title, String message, Long entityId, String entityType, Long branchId, Long organizationId, String metadata);
    void checkProductStock(Product product);
    void checkBatchExpiry(Batch batch);
    void notifySaleCreated(Long saleId, String customer, java.math.BigDecimal amount, Long branchId);
    void notifyPurchaseCreated(Long purchaseId, java.math.BigDecimal amount, Long branchId);
}