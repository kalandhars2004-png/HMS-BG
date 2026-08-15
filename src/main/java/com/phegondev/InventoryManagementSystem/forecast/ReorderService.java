package com.phegondev.InventoryManagementSystem.forecast;

import java.util.List;

public interface ReorderService {
    ReorderPointDTO setReorderPoint(ReorderPointDTO dto);
    ReorderPointDTO getReorderPoint(Long productId);
    List<ReorderPointDTO> getAllReorderPoints();
    List<ReorderPointDTO> getNeedsReorder();
    List<ForecastResult> getForecast();
    void delete(Long id);
}
