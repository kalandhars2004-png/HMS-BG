package com.phegondev.InventoryManagementSystem.kitting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KitItemRepository extends JpaRepository<KitItem, Long> {

    List<KitItem> findByKitId(Long kitId);

    void deleteByKitId(Long kitId);
}
