package com.phegondev.InventoryManagementSystem.invoice;

import com.phegondev.InventoryManagementSystem.common.Response;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createInvoice(@RequestBody @Valid InvoiceDTO invoiceDTO) {
        return ResponseEntity.ok(invoiceService.createInvoice(invoiceDTO));
    }

    @PostMapping("/from-so/{salesOrderId}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> generateFromSalesOrder(@PathVariable Long salesOrderId) {
        return ResponseEntity.ok(invoiceService.generateFromSalesOrder(salesOrderId));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateInvoiceStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(invoiceService.updateInvoiceStatus(id, body.get("status")));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteInvoice(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.deleteInvoice(id));
    }
}

