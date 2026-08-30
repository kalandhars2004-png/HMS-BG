package com.phegondev.InventoryManagementSystem.stockmovement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query("SELECT sm FROM StockMovement sm WHERE " +
           "(:searchText IS NULL OR :searchText = '' OR " +
           "LOWER(sm.productName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.productSku) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.batchNo) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.changedBy) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "ORDER BY sm.createdAt DESC")
    Page<StockMovement> searchMovements(@Param("searchText") String searchText, Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.branchId = :branchId AND " +
           "(:searchText IS NULL OR :searchText = '' OR " +
           "LOWER(sm.productName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.productSku) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.batchNo) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.changedBy) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "ORDER BY sm.createdAt DESC")
    Page<StockMovement> searchMovementsByBranch(@Param("branchId") Long branchId, @Param("searchText") String searchText, Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE " +
           "(:searchText IS NULL OR :searchText = '' OR " +
           "LOWER(sm.productName) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.productSku) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.batchNo) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(sm.changedBy) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "ORDER BY sm.createdAt DESC")
    List<StockMovement> searchMovementsList(@Param("searchText") String searchText);
}
