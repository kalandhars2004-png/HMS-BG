package com.phegondev.InventoryManagementSystem.pos;

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
public class POSSessionDTO {

    private Long id;

    private String sessionNumber;

    private Long openedBy;

    private String openedByName;

    private Long closedBy;

    private String closedByName;

    private String status;

    private BigDecimal openingBalance;

    private BigDecimal closingBalance;

    private BigDecimal totalSales;

    private BigDecimal totalRefunds;

    private BigDecimal netAmount;

    private LocalDateTime openedAt;

    private LocalDateTime closedAt;

    private String notes;
}
