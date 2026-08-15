package com.phegondev.InventoryManagementSystem.purchaseorder;

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
public class PurchaseOrderDTO {

    private Long id;

    private String poNumber;

    private Long supplierId;

    private String supplierName;

    private BigDecimal totalAmount;

    private String status;

    private String paymentTerms;

    private LocalDateTime orderDate;

    private LocalDateTime expectedDelivery;

    private LocalDateTime createdAt;

    private List<PurchaseOrderItemDTO> items;
}
