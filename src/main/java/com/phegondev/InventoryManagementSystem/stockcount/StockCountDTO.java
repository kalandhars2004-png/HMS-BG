package com.phegondev.InventoryManagementSystem.stockcount;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockCountDTO {
    private Long id;
    private String countNumber;
    private Long warehouseId;
    private String warehouseName;
    private String status;
    private Integer totalItems;
    private Integer countedItems;
    private Integer discrepancyCount;
    private String notes;
    private LocalDateTime scheduledDate;
    private LocalDateTime completedDate;
    private LocalDateTime createdAt;
    private Long createdBy;
    private Long completedBy;
    private List<StockCountItemDTO> items;
}
