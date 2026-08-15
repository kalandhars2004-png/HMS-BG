package com.phegondev.InventoryManagementSystem.rack;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RackDTO {
    private Long id;
    @NotBlank(message = "Rack code is required")
    private String code;
    @NotBlank(message = "Category is required")
    private String category;
    private Integer rowsCount;
    private Integer columns;
    private Integer bins;
    private Integer alertThreshold;
    private String temperature;
    private String status;
    private Integer assignedMedicines;
    private Integer capacityPercent;
    private LocalDateTime createdAt;
}
