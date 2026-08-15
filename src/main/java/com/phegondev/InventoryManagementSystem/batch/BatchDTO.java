package com.phegondev.InventoryManagementSystem.batch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
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
public class BatchDTO {
    private Long id;
    private Long productId;
    private String productName;
    @NotBlank(message = "Batch number is required")
    private String batchNo;
    private Integer quantity;
    private BigDecimal mrp;
    private BigDecimal purchasePrice;
    private LocalDateTime manufacturingDate;
    private LocalDateTime expiryDate;
    private Boolean status;
    private LocalDateTime createdAt;
}
