package com.phegondev.InventoryManagementSystem.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByBranchId(Long branchId);
    List<Inventory> findByBranchIdAndWarehouseId(Long branchId, Long warehouseId);
    List<Inventory> findByBranchIdAndProductId(Long branchId, Long productId);
    Optional<Inventory> findByBranchIdAndWarehouseIdAndProductIdAndBatchId(Long branchId, Long warehouseId, Long productId, Long batchId);
    Optional<Inventory> findByBranchIdAndProductIdAndBatchId(Long branchId, Long productId, Long batchId);

    @Query("SELECT COALESCE(SUM(i.quantityOnHand),0) FROM Inventory i WHERE i.branchId = :branchId")
    Long sumQuantityByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT COALESCE(SUM(i.quantityOnHand),0) FROM Inventory i WHERE i.organizationId = :orgId")
    Long sumQuantityByOrganizationId(@Param("orgId") Long orgId);

    List<Inventory> findByProductIdAndQuantityOnHandGreaterThan(Long productId, Integer qty);
}
