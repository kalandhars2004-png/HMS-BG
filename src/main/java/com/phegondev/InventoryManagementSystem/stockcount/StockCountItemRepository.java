package com.phegondev.InventoryManagementSystem.stockcount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockCountItemRepository extends JpaRepository<StockCountItem, Long> {
    List<StockCountItem> findByStockCountId(Long stockCountId);
}
