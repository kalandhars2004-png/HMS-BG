package com.phegondev.InventoryManagementSystem.pos;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
public class POSController {

    private final POSService posService;

    @PostMapping("/sessions/open")
    public ResponseEntity<Response> openSession(@RequestBody POSSessionDTO sessionDTO) {
        return ResponseEntity.ok(posService.openSession(sessionDTO));
    }

    @PutMapping("/sessions/{id}/close")
    public ResponseEntity<Response> closeSession(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(posService.closeSession(id, body.get("closingBalance")));
    }

    @GetMapping("/sessions/active")
    public ResponseEntity<Response> getActiveSession() {
        return ResponseEntity.ok(posService.getActiveSession());
    }

    @GetMapping("/sessions")
    public ResponseEntity<Response> getAllSessions() {
        return ResponseEntity.ok(posService.getAllSessions());
    }

    @PostMapping("/sessions/{id}/transaction")
    public ResponseEntity<Response> addTransaction(@PathVariable Long id, @RequestBody POSTransactionDTO transactionDTO) {
        return ResponseEntity.ok(posService.addTransaction(id, transactionDTO));
    }

    @GetMapping("/sessions/{id}/transactions")
    public ResponseEntity<Response> getSessionTransactions(@PathVariable Long id) {
        return ResponseEntity.ok(posService.getSessionTransactions(id));
    }

    @PutMapping("/transactions/{id}/void")
    public ResponseEntity<Response> voidTransaction(@PathVariable Long id) {
        return ResponseEntity.ok(posService.voidTransaction(id));
    }

    @GetMapping("/daily-sales")
    public ResponseEntity<Response> getDailySales() {
        return ResponseEntity.ok(posService.getDailySales());
    }
}
