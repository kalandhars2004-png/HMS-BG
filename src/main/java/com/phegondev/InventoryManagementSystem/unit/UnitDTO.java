package com.phegondev.InventoryManagementSystem.unit;

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
public class UnitDTO {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    private String shortName;
    private String pluralName;
    private String symbol;
    private String description;
    private String category;
    private String unitType;
    private String measurementSystem;
    private String baseUnit;
    private BigDecimal conversionValue;
    private BigDecimal conversionRate;
    private Integer decimalPlaces;
    private String displayFormat;
    private String applicableFor;
    private Boolean isDefault;
    private Boolean usesPurchases;
    private Boolean usesSales;
    private Boolean usesInventory;
    private Boolean usesManufacturing;
    private Boolean usesPrescriptions;
    private Boolean usesReports;
    private Boolean usesBarcodePrinting;
    private Boolean status;
    private LocalDateTime createdAt;
}
