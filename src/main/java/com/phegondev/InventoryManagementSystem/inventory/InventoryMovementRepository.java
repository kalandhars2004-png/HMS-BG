package com.phegondev.InventoryManagementSystem.inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findAllByOrderByCreatedAtDesc();
    List<InventoryMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
}