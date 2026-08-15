package com.phegondev.InventoryManagementSystem.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByReadFalseOrderByCreatedAtDesc();
    List<Alert> findAllByOrderByCreatedAtDesc();
    long countByReadFalse();
    Optional<Alert> findFirstByTypeAndRelatedEntityIdAndReadFalseOrderByCreatedAtDesc(String type, Long relatedEntityId);
}