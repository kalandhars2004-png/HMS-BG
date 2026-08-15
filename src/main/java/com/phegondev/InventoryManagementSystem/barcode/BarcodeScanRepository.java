package com.phegondev.InventoryManagementSystem.barcode;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BarcodeScanRepository extends JpaRepository<BarcodeScan, Long> {

    List<BarcodeScan> findByBarcode(String barcode);
}
