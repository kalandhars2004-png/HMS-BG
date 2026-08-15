package com.phegondev.InventoryManagementSystem.purchasereturn;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface PurchaseReturnService {
    Response createPurchaseReturn(PurchaseReturnDTO purchaseReturnDTO);
    Response getAllPurchaseReturns();
    Response getPurchaseReturnById(Long id);
    Response updatePurchaseReturn(Long id, PurchaseReturnDTO purchaseReturnDTO);
    Response deletePurchaseReturn(Long id);
}
