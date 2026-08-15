package com.phegondev.InventoryManagementSystem.purchasereturn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class PurchaseReturnDTO {
    private Long id;
    @NotNull(message = "Product id is required")
    private Long productId;
    @NotNull(message = "Supplier id is required")
    private Long supplierId;
    @NotNull(message = "Quantity is required")
    private Integer quantity;
    @NotBlank(message = "Reason is required")
    private String reason;
    @NotNull(message = "Return amount is required")
    private BigDecimal returnAmount;
    private String status;
    private LocalDateTime createdAt;
}
