package com.phegondev.InventoryManagementSystem.kitting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class KitDTO {

    private Long id;

    private String name;

    private String sku;

    private String description;

    private BigDecimal sellingPrice;

    private BigDecimal costPrice;

    private Integer quantityOnHand;

    private String status;

    private LocalDateTime createdAt;

    private List<KitItemDTO> items;
}
