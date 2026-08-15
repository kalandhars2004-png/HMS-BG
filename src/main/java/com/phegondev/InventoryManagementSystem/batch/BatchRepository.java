package com.phegondev.InventoryManagementSystem.batch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    List<Batch> findByExpiryDateBefore(LocalDateTime date);
    List<Batch> findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(Long productId, Integer quantity);
}
