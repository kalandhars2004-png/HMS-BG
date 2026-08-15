package com.phegondev.InventoryManagementSystem.barcode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BarcodeScanRequest {

    private String barcode;

    private String action;

    private Integer quantity;

    private String location;
}
