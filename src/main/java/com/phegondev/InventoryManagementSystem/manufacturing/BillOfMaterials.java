package com.phegondev.InventoryManagementSystem.manufacturing;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bill_of_materials")
public class BillOfMaterials {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name = "finished_product_id")
    private Long finishedProductId;

    @Column(name = "output_quantity")
    private Integer outputQuantity;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
