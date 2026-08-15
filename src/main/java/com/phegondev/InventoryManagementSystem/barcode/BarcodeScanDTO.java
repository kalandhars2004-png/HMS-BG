package com.phegondev.InventoryManagementSystem.barcode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BarcodeScanDTO {

    private Long id;

    private String barcode;

    private Long productId;

    private String productName;

    private String action;

    private Integer quantity;

    private String location;

    private LocalDateTime createdAt;
}
