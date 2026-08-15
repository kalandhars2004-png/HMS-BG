package com.phegondev.InventoryManagementSystem.category;

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
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String slug;

    private String description;

    private Boolean status;

    private LocalDateTime createdAt;

    private String code;
    private String icon;
    private String color;
    private Integer displayOrder;
    private Long parentId;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
