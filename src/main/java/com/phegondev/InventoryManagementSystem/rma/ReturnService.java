package com.phegondev.InventoryManagementSystem.rma;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface ReturnService {
    Response createReturn(ReturnRequestDTO returnRequestDTO);
    Response getAllReturns();
    Response getReturnById(Long id);
    Response updateStatus(Long id, String status);
    Response updateDisposition(Long returnId, Long itemId, String disposition);
    Response deleteReturn(Long id);
}
