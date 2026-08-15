package com.phegondev.InventoryManagementSystem.unit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitFilter {
    private int page = 1;
    private int limit = 20;

    private String search;
    private String status;
    private String category;
    private String measurementSystem;
    private String baseUnit;
    private List<String> usages;

    private String sort;
    private String direction;

    private LocalDate fromDate;
    private LocalDate toDate;

    private LocalDate updatedFromDate;
    private LocalDate updatedToDate;

    private BigDecimal minConversionRate;
    private BigDecimal maxConversionRate;

    private String createdBy;
    private Boolean archived;
}