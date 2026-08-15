package com.phegondev.InventoryManagementSystem.barcode;

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
@Table(name = "barcode_scans")
public class BarcodeScan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String barcode;

    private Long productId;

    private String action;

    private Integer quantity;

    private String location;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
