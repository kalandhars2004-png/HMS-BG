package com.phegondev.InventoryManagementSystem.equipment;

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
public class EquipmentDTO {
    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    private String model;
    private String category;
    private String serialNo;
    private String status;
    private String lastService;
    private String nextService;
    private String location;
    private LocalDateTime createdAt;
}
