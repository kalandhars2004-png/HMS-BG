package com.phegondev.InventoryManagementSystem.invoice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InvoiceDTO {

    private Long id;

    private String invoiceNumber;

    private Long salesOrderId;

    private Long transactionId;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    private String customerAddress;

    private BigDecimal subtotal;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private String status;

    private String paymentMethod;

    private String notes;

    private LocalDateTime invoiceDate;

    private LocalDateTime dueDate;

    private LocalDateTime createdAt;

    private Long createdBy;

    private List<InvoiceItemDTO> items;
}
