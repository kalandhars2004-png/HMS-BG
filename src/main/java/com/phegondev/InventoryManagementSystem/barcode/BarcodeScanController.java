package com.phegondev.InventoryManagementSystem.barcode;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/barcode")
@RequiredArgsConstructor
public class BarcodeScanController {

    private final BarcodeScanService barcodeScanService;

    @PostMapping("/scan")
    public ResponseEntity<Response> scanBarcode(@RequestBody BarcodeScanRequest request) {
        return ResponseEntity.ok(barcodeScanService.scanBarcode(request));
    }

    @GetMapping("/lookup/{barcode}")
    public ResponseEntity<Response> lookupProduct(@PathVariable String barcode) {
        return ResponseEntity.ok(barcodeScanService.lookupProduct(barcode));
    }

    @GetMapping("/scans")
    public ResponseEntity<Response> getAllScans() {
        return ResponseEntity.ok(barcodeScanService.getAllScans());
    }
}
