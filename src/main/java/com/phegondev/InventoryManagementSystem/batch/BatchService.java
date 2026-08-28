package com.phegondev.InventoryManagementSystem.batch;

import com.phegondev.InventoryManagementSystem.common.Response;

import java.time.LocalDateTime;

public interface BatchService {
    Response createBatch(BatchDTO batchDTO);

    /** Legacy full list; paged when both page and size are provided. */
    default Response getAllBatches() {
        return getAllBatches(null, null);
    }

    Response getAllBatches(Integer page, Integer size);
    Response getBatchById(Long id);
    Response updateBatch(Long id, BatchDTO batchDTO);
    Response deleteBatch(Long id);
    Response getBatchesExpiringBefore(LocalDateTime date);
}
