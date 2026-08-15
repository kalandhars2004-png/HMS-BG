package com.phegondev.InventoryManagementSystem.batch;

import com.phegondev.InventoryManagementSystem.common.Response;

import java.time.LocalDateTime;

public interface BatchService {
    Response createBatch(BatchDTO batchDTO);
    Response getAllBatches();
    Response getBatchById(Long id);
    Response updateBatch(Long id, BatchDTO batchDTO);
    Response deleteBatch(Long id);
    Response getBatchesExpiringBefore(LocalDateTime date);
}
