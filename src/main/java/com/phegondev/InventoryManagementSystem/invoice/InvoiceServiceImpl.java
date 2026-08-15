package com.phegondev.InventoryManagementSystem.invoice;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.salesorder.SalesOrder;
import com.phegondev.InventoryManagementSystem.salesorder.SalesOrderItem;
import com.phegondev.InventoryManagementSystem.salesorder.SalesOrderItemRepository;
import com.phegondev.InventoryManagementSystem.salesorder.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response createInvoice(InvoiceDTO invoiceDTO) {
        Invoice invoice = modelMapper.map(invoiceDTO, Invoice.class);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setCreatedAt(LocalDateTime.now());
        Invoice saved = invoiceRepository.save(invoice);

        if (invoiceDTO.getItems() != null) {
            for (InvoiceItemDTO itemDTO : invoiceDTO.getItems()) {
                InvoiceItem item = modelMapper.map(itemDTO, InvoiceItem.class);
                item.setInvoiceId(saved.getId());
                invoiceItemRepository.save(item);
            }
        }

        InvoiceDTO dto = mapInvoiceWithItems(saved);

        return Response.builder()
                .status(200)
                .message("Invoice created successfully")
                .invoice(dto)
                .build();
    }

    @Override
    public Response getAllInvoices() {
        List<Invoice> invoices = invoiceRepository.findAllByOrderByCreatedAtDesc();
        List<InvoiceDTO> invoiceDTOS = invoices.stream()
                .map(this::mapInvoiceWithItems)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("success")
                .invoices(invoiceDTOS)
                .build();
    }

    @Override
    public Response getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice Not Found"));
        InvoiceDTO dto = mapInvoiceWithItems(invoice);

        return Response.builder()
                .status(200)
                .message("success")
                .invoice(dto)
                .build();
    }

    @Override
    @Transactional
    public Response updateInvoiceStatus(Long id, String status) {
        Invoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice Not Found"));
        existing.setStatus(status);
        invoiceRepository.save(existing);

        return Response.builder()
                .status(200)
                .message("Invoice status updated successfully")
                .build();
    }

    @Override
    @Transactional
    public Response generateFromSalesOrder(Long salesOrderId) {
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new NotFoundException("Sales Order Not Found"));

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .salesOrderId(salesOrderId)
                .customerName(salesOrder.getCustomerName())
                .customerEmail(salesOrder.getCustomerEmail())
                .customerPhone(salesOrder.getCustomerPhone())
                .customerAddress(salesOrder.getCustomerAddress())
                .subtotal(salesOrder.getSubtotal())
                .taxAmount(salesOrder.getTaxAmount())
                .discountAmount(salesOrder.getDiscountAmount())
                .totalAmount(salesOrder.getTotalAmount())
                .status("UNPAID")
                .paymentMethod(salesOrder.getPaymentMethod())
                .notes(salesOrder.getNotes())
                .invoiceDate(LocalDateTime.now())
                .createdBy(salesOrder.getCreatedBy())
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        List<SalesOrderItem> soItems = salesOrderItemRepository.findBySalesOrderId(salesOrderId);
        for (SalesOrderItem soItem : soItems) {
            InvoiceItem item = InvoiceItem.builder()
                    .invoiceId(saved.getId())
                    .productId(soItem.getProductId())
                    .quantity(soItem.getQuantity())
                    .unitPrice(soItem.getUnitPrice())
                    .totalPrice(soItem.getTotalPrice())
                    .build();
            invoiceItemRepository.save(item);
        }

        return Response.builder()
                .status(200)
                .message("Invoice generated from Sales Order successfully")
                .invoice(modelMapper.map(saved, InvoiceDTO.class))
                .build();
    }

    @Override
    @Transactional
    public Response deleteInvoice(Long id) {
        invoiceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Invoice Not Found"));

        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(id);
        invoiceItemRepository.deleteAll(items);
        invoiceRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Invoice Successfully Deleted")
                .build();
    }

    private InvoiceDTO mapInvoiceWithItems(Invoice invoice) {
        InvoiceDTO dto = modelMapper.map(invoice, InvoiceDTO.class);
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(invoice.getId());
        List<InvoiceItemDTO> itemDTOS = items.stream()
                .map(item -> modelMapper.map(item, InvoiceItemDTO.class))
                .collect(Collectors.toList());
        dto.setItems(itemDTOS);
        return dto;
    }

    private String generateInvoiceNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = invoiceRepository.count() + 1;
        return "INV-" + year + "-" + String.format("%04d", count);
    }
}
