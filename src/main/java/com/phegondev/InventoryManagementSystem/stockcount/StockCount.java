package com.phegondev.InventoryManagementSystem.stockcount;

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
@Table(name = "stock_counts")
public class StockCount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "count_number", unique = true)
    private String countNumber;

    @Column(name = "warehouse_id")
    private Long warehouseId;

    private String status;

    @Column(name = "total_items")
    private Integer totalItems;

    @Column(name = "counted_items")
    private Integer countedItems;

    @Column(name = "discrepancy_count")
    private Integer discrepancyCount;

    private String notes;

    @Column(name = "scheduled_date")
    private LocalDateTime scheduledDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "completed_by")
    private Long completedBy;
}
