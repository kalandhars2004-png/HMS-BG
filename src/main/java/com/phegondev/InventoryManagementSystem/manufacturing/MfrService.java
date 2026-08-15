package com.phegondev.InventoryManagementSystem.manufacturing;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface MfrService {

    Response addBOM(BillOfMaterialsDTO billOfMaterialsDTO);

    Response getAllBOMs();

    Response getBOMById(Long id);

    Response updateBOM(Long id, BillOfMaterialsDTO billOfMaterialsDTO);

    Response deleteBOM(Long id);

    Response addProductionOrder(ProductionOrderDTO productionOrderDTO);

    Response getAllProductionOrders();

    Response getProductionOrderById(Long id);

    Response updateProductionOrderStatus(Long id, String status);

    Response startProduction(Long id);

    Response completeProduction(Long id);
}
