package com.phegondev.InventoryManagementSystem.stocktransfer;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface StockTransferService {
    Response createStockTransfer(StockTransferDTO stockTransferDTO);
    Response getAllStockTransfers();
    Response getStockTransferById(Long id);
    Response updateStockTransfer(Long id, StockTransferDTO stockTransferDTO);
    Response deleteStockTransfer(Long id);
}
