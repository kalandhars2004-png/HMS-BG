package com.phegondev.InventoryManagementSystem.manufacturing;

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
public class BillOfMaterialsDTO {

    private Long id;

    private String name;

    private String description;

    private Long finishedProductId;

    private String finishedProductName;

    private Integer outputQuantity;

    private String status;

    private LocalDateTime createdAt;

    private List<BOMItemDTO> items;
}
