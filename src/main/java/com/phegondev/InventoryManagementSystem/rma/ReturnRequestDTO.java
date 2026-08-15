package com.phegondev.InventoryManagementSystem.rma;

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
public class ReturnRequestDTO {
    private Long id;
    private String returnNumber;
    private Long salesOrderId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String reason;
    private String type;
    private String status;
    private String condition;
    private BigDecimal refundAmount;
    private String notes;
    private LocalDateTime requestDate;
    private LocalDateTime receivedDate;
    private LocalDateTime completedDate;
    private LocalDateTime createdAt;
    private List<ReturnItemDTO> items;
}
