package com.phegondev.InventoryManagementSystem.manufacturing;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BOMItemRepository extends JpaRepository<BOMItem, Long> {

    List<BOMItem> findByBomId(Long bomId);

    void deleteByBomId(Long bomId);
}
