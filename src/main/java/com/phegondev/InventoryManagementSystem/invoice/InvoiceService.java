package com.phegondev.InventoryManagementSystem.invoice;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface InvoiceService {
    Response createInvoice(InvoiceDTO invoiceDTO);
    Response getAllInvoices();
    Response getInvoiceById(Long id);
    Response updateInvoiceStatus(Long id, String status);
    Response generateFromSalesOrder(Long salesOrderId);
    Response deleteInvoice(Long id);
}
