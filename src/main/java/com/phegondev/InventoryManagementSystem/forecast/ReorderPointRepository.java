package com.phegondev.InventoryManagementSystem.forecast;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReorderPointRepository extends JpaRepository<ReorderPoint, Long> {
    Optional<ReorderPoint> findByProductId(Long productId);
    List<ReorderPoint> findByStatus(String status);
}
