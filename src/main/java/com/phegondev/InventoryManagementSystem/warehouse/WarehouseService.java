package com.phegondev.InventoryManagementSystem.warehouse;

import com.phegondev.InventoryManagementSystem.warehouse.WarehouseDTO;
import com.phegondev.InventoryManagementSystem.common.Response;

public interface WarehouseService {
    Response createWarehouse(WarehouseDTO warehouseDTO);
    Response getAllWarehouses();
    Response getWarehouseById(Long id);
    Response updateWarehouse(Long id, WarehouseDTO warehouseDTO);
    Response deleteWarehouse(Long id);
}
