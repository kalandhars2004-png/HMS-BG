package com.phegondev.InventoryManagementSystem.rack;

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
@Table(name = "racks", uniqueConstraints = @UniqueConstraint(columnNames = "code"))
public class Rack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private String category;
    private Integer rowsCount;
    private Integer columns;
    private Integer bins;
    @Column(name = "alert_threshold")
    private Integer alertThreshold;
    private String temperature;
    private String status;
    @Column(name = "assigned_medicines")
    private Integer assignedMedicines;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}