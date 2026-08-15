package com.phegondev.InventoryManagementSystem.manufacturing;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
}
