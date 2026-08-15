package com.phegondev.InventoryManagementSystem.purchaseorder;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface PurchaseOrderService {

    Response addPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO);

    Response getAllPurchaseOrders();

    Response getPurchaseOrderById(Long id);

    Response updateStatus(Long id, String status);

    Response receiveItems(Long id);

    Response deletePurchaseOrder(Long id);
}
