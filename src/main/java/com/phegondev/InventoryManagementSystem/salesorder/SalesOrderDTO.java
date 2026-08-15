package com.phegondev.InventoryManagementSystem.salesorder;

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
public class SalesOrderDTO {

    private Long id;

    private String soNumber;

    private Long customerId;

    private String customerName;

    private String customerEmail;

    private String customerPhone;

    private String customerAddress;

    private BigDecimal subtotal;

    private BigDecimal taxAmount;

    private BigDecimal discountAmount;

    private BigDecimal totalAmount;

    private String status;

    private String paymentStatus;

    private String paymentMethod;

    private String notes;

    private LocalDateTime orderDate;

    private LocalDateTime createdAt;

    private Long createdBy;

    private List<SalesOrderItemDTO> items;
}
