package com.phegondev.InventoryManagementSystem.pos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface POSTransactionRepository extends JpaRepository<POSTransaction, Long> {
    List<POSTransaction> findBySessionId(Long sessionId);
    List<POSTransaction> findAllByOrderByCreatedAtDesc();
}
