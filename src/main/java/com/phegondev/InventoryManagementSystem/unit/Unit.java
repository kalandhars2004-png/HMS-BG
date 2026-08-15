package com.phegondev.InventoryManagementSystem.unit;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "units")
public class Unit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    @Column(name = "short_name")
    private String shortName;
    private String pluralName;
    private String symbol;
    private String description;
    private String category;
    @Column(name = "unit_type")
    private String unitType;
    @Column(name = "measurement_system")
    private String measurementSystem;
    @Column(name = "base_unit")
    private String baseUnit;
    @Column(name = "conversion_value")
    private BigDecimal conversionValue;
    @Column(name = "conversion_rate")
    private BigDecimal conversionRate;
    @Column(name = "decimal_places")
    private Integer decimalPlaces;
    @Column(name = "display_format")
    private String displayFormat;
    @Column(name = "applicable_for")
    private String applicableFor;
    @Column(name = "is_default")
    private Boolean isDefault;
    @Column(name = "uses_purchases")
    private Boolean usesPurchases;
    @Column(name = "uses_sales")
    private Boolean usesSales;
    @Column(name = "uses_inventory")
    private Boolean usesInventory;
    @Column(name = "uses_manufacturing")
    private Boolean usesManufacturing;
    @Column(name = "uses_prescriptions")
    private Boolean usesPrescriptions;
    @Column(name = "uses_reports")
    private Boolean usesReports;
    @Column(name = "uses_barcode_printing")
    private Boolean usesBarcodePrinting;
    private Boolean status;
    @Column(name = "archived")
    private Boolean archived;
    @Column(name = "usage_count")
    private Integer usageCount;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
