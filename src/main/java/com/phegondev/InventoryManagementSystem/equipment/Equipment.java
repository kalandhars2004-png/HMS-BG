package com.phegondev.InventoryManagementSystem.equipment;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "equipment")
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    private String name;
    private String model;
    private String category;
    @Column(name = "serial_no")
    private String serialNo;
    private String status;
    @Column(name = "last_service")
    private String lastService;
    @Column(name = "next_service")
    private String nextService;
    private String location;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
