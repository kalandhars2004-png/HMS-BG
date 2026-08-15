package com.phegondev.InventoryManagementSystem.alert;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface AlertService {
    Response checkAndCreateAlerts();
    Response getUnreadAlerts();
    Response getAllAlerts();
    Response markAsRead(Long id);
    Response markAllAsRead();
    Response getUnreadCount();
}