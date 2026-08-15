package com.phegondev.InventoryManagementSystem.forecast;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reorder")
@RequiredArgsConstructor
public class ReorderController {

    private final ReorderService reorderService;

    @PostMapping("/points/add")
    public ResponseEntity<Response> setReorderPoint(@RequestBody ReorderPointDTO dto) {
        ReorderPointDTO result = reorderService.setReorderPoint(dto);
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Reorder point set successfully")
                .reorderPoint(result)
                .build());
    }

    @GetMapping("/points")
    public ResponseEntity<Response> getAllReorderPoints() {
        List<ReorderPointDTO> points = reorderService.getAllReorderPoints();
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("success")
                .reorderPoints(points)
                .build());
    }

    @GetMapping("/points/{productId}")
    public ResponseEntity<Response> getReorderPoint(@PathVariable Long productId) {
        ReorderPointDTO point = reorderService.getReorderPoint(productId);
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("success")
                .reorderPoint(point)
                .build());
    }

    @GetMapping("/needs-reorder")
    public ResponseEntity<Response> getNeedsReorder() {
        List<ReorderPointDTO> points = reorderService.getNeedsReorder();
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("success")
                .reorderPoints(points)
                .build());
    }

    @GetMapping("/forecast")
    public ResponseEntity<Response> getForecast() {
        List<ForecastResult> results = reorderService.getForecast();
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("success")
                .forecastResults(results)
                .build());
    }

    @DeleteMapping("/points/{id}")
    public ResponseEntity<Response> deleteReorderPoint(@PathVariable Long id) {
        reorderService.delete(id);
        return ResponseEntity.ok(Response.builder()
                .status(200)
                .message("Reorder point deleted successfully")
                .build());
    }
}
