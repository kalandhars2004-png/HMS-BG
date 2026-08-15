package com.phegondev.InventoryManagementSystem.manufacturing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bom_items")
public class BOMItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bom_id")
    private Long bomId;

    @Column(name = "raw_material_id")
    private Long rawMaterialId;

    private Integer quantity;

    private String unit;
}
