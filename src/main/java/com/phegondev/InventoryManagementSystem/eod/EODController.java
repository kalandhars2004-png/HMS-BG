package com.phegondev.InventoryManagementSystem.eod;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/eod")
@RequiredArgsConstructor
public class EODController {

    private final EODService eodService;

    @PostMapping("/generate")
    public ResponseEntity<Response> generateToday() {
        return ResponseEntity.ok(eodService.generateReport());
    }

    @PostMapping("/generate/{date}")
    public ResponseEntity<Response> generateForDate(@PathVariable String date) {
        return ResponseEntity.ok(eodService.generateReportForDate(LocalDate.parse(date)));
    }

    @GetMapping("/report/{date}")
    public ResponseEntity<Response> getReport(@PathVariable String date) {
        return ResponseEntity.ok(eodService.getReport(LocalDate.parse(date)));
    }
}