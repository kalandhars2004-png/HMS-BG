package com.phegondev.InventoryManagementSystem.alert;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByReadFalseOrderByCreatedAtDesc();
    List<Alert> findAllByOrderByCreatedAtDesc();
    long countByReadFalse();
    Optional<Alert> findFirstByTypeAndRelatedEntityIdAndReadFalseOrderByCreatedAtDesc(String type, Long relatedEntityId);

    // Branch & user aware — for RBAC and deduplication
    List<Alert> findByBranchIdAndReadFalseOrderByCreatedAtDesc(Long branchId);
    List<Alert> findByBranchIdOrderByCreatedAtDesc(Long branchId);
    long countByBranchIdAndReadFalse(Long branchId);
    long countByUserIdAndReadFalse(Long userId);

    Page<Alert> findByBranchId(Long branchId, Pageable pageable);
    Page<Alert> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Alert> findFirstByTypeAndRelatedEntityIdAndBranchIdAndIsResolvedFalse(String type, Long relatedEntityId, Long branchId);
    Optional<Alert> findFirstByTypeAndRelatedEntityIdAndBranchIdAndReadFalse(String type, Long relatedEntityId, Long branchId);

    @Query("SELECT a FROM Alert a WHERE (:branchId IS NULL OR a.branchId = :branchId) AND (:unreadOnly = false OR a.read = false) AND (:type IS NULL OR a.type = :type) ORDER BY a.createdAt DESC")
    Page<Alert> findFiltered(Long branchId, Boolean unreadOnly, String type, Pageable pageable);
}