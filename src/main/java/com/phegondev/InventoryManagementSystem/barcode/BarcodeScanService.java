package com.phegondev.InventoryManagementSystem.barcode;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface BarcodeScanService {

    Response scanBarcode(BarcodeScanRequest request);

    Response getByBarcode(String barcode);

    Response getAllScans();

    Response lookupProduct(String barcode);
}
