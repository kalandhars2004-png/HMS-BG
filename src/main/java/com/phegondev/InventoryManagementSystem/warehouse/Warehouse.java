package com.phegondev.InventoryManagementSystem.warehouse;

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
@Table(name = "warehouses")
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String warehouse;
    @Column(name = "contact_person")
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    @Column(name = "postal_code")
    private String postalCode;
    @Column(name = "total_products")
    private Integer totalProducts;
    private Integer stock;
    private Integer qty;
    private Boolean status;
    @Column(name = "created_on")
    private String createdOn;
}
