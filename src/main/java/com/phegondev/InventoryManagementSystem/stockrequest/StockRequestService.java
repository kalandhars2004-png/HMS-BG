package com.phegondev.InventoryManagementSystem.stockrequest;

import java.util.List;

public interface StockRequestService {
    StockRequestDTO create(StockRequestDTO dto);
    StockRequestDTO approve(Long id);
    StockRequestDTO reject(Long id);
    StockRequestDTO ship(Long id);
    StockRequestDTO receive(Long id);
    StockRequestDTO cancel(Long id);
    List<StockRequestDTO> getForCurrentBranch();
    List<StockRequestDTO> getAll(); // super admin
    StockRequestDTO getById(Long id);
}
