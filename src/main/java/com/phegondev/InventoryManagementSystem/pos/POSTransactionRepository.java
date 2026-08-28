package com.phegondev.InventoryManagementSystem.pos;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface POSTransactionRepository extends JpaRepository<POSTransaction, Long> {
    List<POSTransaction> findBySessionId(Long sessionId);
    List<POSTransaction> findAllByOrderByCreatedAtDesc();

    // EOD used to load every POS transaction ever and filter by date/status in Java;
    // this pushes that filter into the database.
    List<POSTransaction> findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            String status, LocalDateTime start, LocalDateTime end);
}
