package com.phegondev.InventoryManagementSystem.stockmovement;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface StockMovementService {
    void record(Long productId, String productName, String productSku, String batchNo,
                MovementType movementType, Integer quantityIn, Integer quantityOut,
                Integer balanceStock, Long referenceId, String referenceType, String changedBy);
    Response getAll(int page, int size, String searchText);
}
