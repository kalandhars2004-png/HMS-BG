package com.phegondev.InventoryManagementSystem.product;

import com.phegondev.InventoryManagementSystem.category.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "products", indexes = {
        // POS barcode lookup, warehouse-scoped listings and category joins.
        @Index(name = "idx_products_barcode", columnList = "barcode"),
        @Index(name = "idx_products_warehouse_id", columnList = "warehouse_id"),
        @Index(name = "idx_products_category_id", columnList = "category_id"),
        @Index(name = "idx_products_stock_quantity", columnList = "stock_quantity"),
        @Index(name = "idx_products_branch", columnList = "branch_id"),
        @Index(name = "idx_products_branch_sku", columnList = "branch_id,sku")
})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Sku is required")
    @Column(unique = true)
    private String sku;

    @Positive(message = "Product price msut be a positive value")
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be lesser than zero")
    private Integer stockQuantity;

    private String description;

    private String imageUrl;

    private LocalDateTime expiryDate;

    private LocalDateTime manufacturingDate;

    private  LocalDateTime updatedAt;

    private final LocalDateTime createdAt = LocalDateTime.now();

    private String genericName;
    private String barcode;
    private BigDecimal mrp;
    private BigDecimal purchasePrice;
    private BigDecimal taxPercentage;
    private BigDecimal discountPercentage;
    private Integer lowStockQuantity;
    private Boolean prescriptionRequired;
    private Long brandId;
    private Long unitId;
    private Long variantId;
    private Long warehouseId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "organization_id")
    private Long organizationId;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", expiryDate=" + expiryDate +
                ", updatedAt=" + updatedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
