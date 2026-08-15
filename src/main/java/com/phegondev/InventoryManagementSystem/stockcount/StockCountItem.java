package com.phegondev.InventoryManagementSystem.stockcount;

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
@Table(name = "stock_count_items")
public class StockCountItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_count_id")
    private Long stockCountId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "expected_quantity")
    private Integer expectedQuantity;

    @Column(name = "actual_quantity")
    private Integer actualQuantity;

    private Integer variance;

    private String status;

    private String notes;
}
