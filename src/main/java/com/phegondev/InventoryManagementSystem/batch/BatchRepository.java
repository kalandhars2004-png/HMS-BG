package com.phegondev.InventoryManagementSystem.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByExpiryDateBefore(LocalDateTime date);
    List<Batch> findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(Long productId, Integer quantity);
    List<Batch> findByBranchId(Long branchId);
    List<Batch> findByBranchIdAndProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(Long branchId, Long productId, Integer qty);

    // Expiry counters for EOD/alerts without loading batch rows into memory.
    long countByExpiryDateBetween(LocalDateTime start, LocalDateTime end);
    long countByExpiryDateBefore(LocalDateTime date);

    List<Batch> findByExpiryDateBetween(LocalDateTime start, LocalDateTime end);
}
