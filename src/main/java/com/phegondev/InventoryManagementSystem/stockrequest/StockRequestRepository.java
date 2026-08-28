package com.phegondev.InventoryManagementSystem.stockrequest;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockRequestRepository extends JpaRepository<StockRequest, Long> {
    List<StockRequest> findByBranchIdOrderByRequestedAtDesc(Long branchId);
    List<StockRequest> findByOrganizationIdOrderByRequestedAtDesc(Long organizationId);
    List<StockRequest> findByStatus(StockRequestStatus status);
    List<StockRequest> findByBranchIdAndStatus(Long branchId, StockRequestStatus status);
    List<StockRequest> findBySourceBranchId(Long sourceBranchId);
}
