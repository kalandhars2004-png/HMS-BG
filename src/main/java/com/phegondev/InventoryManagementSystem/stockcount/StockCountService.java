package com.phegondev.InventoryManagementSystem.stockcount;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface StockCountService {
    Response createStockCount(StockCountDTO stockCountDTO);
    Response getAllStockCounts();
    Response getStockCountById(Long id);
    Response updateStockCountStatus(Long id, String status);
    Response addItemToStockCount(Long stockCountId, StockCountItemDTO itemDTO);
    Response updateItemCount(Long stockCountId, Long itemId, Integer actualQuantity);
    Response completeStockCount(Long id);
    Response deleteStockCount(Long id);
}
