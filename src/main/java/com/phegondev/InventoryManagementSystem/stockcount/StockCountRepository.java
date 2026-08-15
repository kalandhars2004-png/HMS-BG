package com.phegondev.InventoryManagementSystem.stockcount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockCountRepository extends JpaRepository<StockCount, Long> {
    List<StockCount> findAllByOrderByCreatedAtDesc();
}
