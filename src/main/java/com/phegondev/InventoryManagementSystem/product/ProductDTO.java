package com.phegondev.InventoryManagementSystem.product;

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
public class ProductDTO {

    private Long id;

    private Long productId;
    private Long categoryId;
    private Long supplierId;

    private String name;

    private String sku;

    private BigDecimal price;

    private Integer stockQuantity;

    private Integer quantity;

    private String categoryName;

    private String description;

    private String imageUrl;

    private LocalDateTime expiryDate;

    private LocalDateTime manufacturingDate;

    private LocalDateTime updatedAt;

    private LocalDateTime createdAt;

    private String genericName;
    private String barcode;
    private BigDecimal mrp;
    private BigDecimal purchasePrice;
    private BigDecimal taxPercentage;
    private BigDecimal discountPercentage;
    private Integer lowStockQuantity;
    private Boolean prescriptionRequired;
    private Long brandId;
    private Long unitId;
    private Long variantId;
    private Long warehouseId;
    private String brandName;
    private String unitName;
    private String variantName;
    private String warehouseName;

}
