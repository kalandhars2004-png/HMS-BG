package com.phegondev.InventoryManagementSystem.pos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class POSTransactionDTO {

    private Long id;
    private Long sessionId;
    private String receiptNumber;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private String status;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private Long billerId;
    private String billerName;
    private LocalDateTime createdAt;
}