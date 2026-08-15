package com.phegondev.InventoryManagementSystem.salesorder;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface SalesOrderService {
    Response createSalesOrder(SalesOrderDTO salesOrderDTO);
    Response getAllSalesOrders();
    Response getSalesOrderById(Long id);
    Response updateSalesOrderStatus(Long id, String status);
    Response deleteSalesOrder(Long id);
    Response updatePaymentStatus(Long id, String paymentStatus);
}
