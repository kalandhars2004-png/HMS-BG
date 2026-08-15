package com.phegondev.InventoryManagementSystem.manufacturing;

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
public class ProductionOrderDTO {

    private Long id;

    private String orderNumber;

    private Long bomId;

    private String bomName;

    private Long finishedProductId;

    private String finishedProductName;

    private Integer plannedQuantity;

    private Integer completedQuantity;

    private String status;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private LocalDateTime createdAt;

    private Long createdBy;
}
