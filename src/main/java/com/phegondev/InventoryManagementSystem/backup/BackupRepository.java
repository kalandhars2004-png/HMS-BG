package com.phegondev.InventoryManagementSystem.backup;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BackupRepository extends JpaRepository<Backup, Long> {
    List<Backup> findTop10ByOrderByCreatedAtDesc();
    List<Backup> findAllByOrderByCreatedAtDesc();
}
