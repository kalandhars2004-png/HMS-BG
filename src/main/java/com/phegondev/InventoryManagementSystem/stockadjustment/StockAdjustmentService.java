package com.phegondev.InventoryManagementSystem.stockadjustment;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface StockAdjustmentService {
    Response createStockAdjustment(StockAdjustmentDTO stockAdjustmentDTO);
    Response getAllStockAdjustments();
    Response getStockAdjustmentById(Long id);
    Response updateStockAdjustment(Long id, StockAdjustmentDTO stockAdjustmentDTO);
    Response deleteStockAdjustment(Long id);
}
