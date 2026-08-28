package com.phegondev.InventoryManagementSystem.product;

import com.phegondev.InventoryManagementSystem.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByWarehouseId(Long warehouseId);
    List<Product> findByBranchId(Long branchId);
    List<Product> findByBranchIdAndWarehouseId(Long branchId, Long warehouseId);
    List<Product> findByBranchIdOrderByIdDesc(Long branchId);
    Optional<Product> findByIdAndBranchId(Long id, Long branchId);
    Optional<Product> findByBarcode(String barcode);
    Optional<Product> findByBarcodeAndBranchId(String barcode, Long branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdWithLock(@Param("id") Long id);

    // Aggregate counters used by EOD/alerts. Previously these loaded every product
    // into memory and counted in Java, which does not scale with catalogue size.

    @Query("SELECT COUNT(p) FROM Product p " +
            "WHERE p.lowStockQuantity IS NOT NULL AND p.stockQuantity > 0 " +
            "AND p.stockQuantity <= p.lowStockQuantity")
    long countLowStock();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.stockQuantity IS NULL OR p.stockQuantity <= 0")
    long countOutOfStock();

    /** Closing stock value: purchase price if set, else selling price, times quantity. */
    @Query("SELECT COALESCE(SUM(COALESCE(p.purchasePrice, p.price, 0) * COALESCE(p.stockQuantity, 0)), 0) FROM Product p")
    BigDecimal sumStockValue();

    List<Product> findByStockQuantityLessThanEqual(Integer quantity);

    /**
     * Two-column projection for bulk item generation (e.g. stock counts). Hydrating
     * full entities just to read id + quantity dominated that flow's runtime.
     */
    @Query("SELECT p.id AS id, p.stockQuantity AS stockQuantity FROM Product p")
    List<ProductStockView> findAllIdAndStockQuantity();

    interface ProductStockView {
        Long getId();
        Integer getStockQuantity();
    }
}
