package com.phegondev.InventoryManagementSystem.branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByCode(String code);
    List<Branch> findByOrganizationId(Long organizationId);
    List<Branch> findByStatus(BranchStatus status);
    List<Branch> findByOrganizationIdAndStatus(Long organizationId, BranchStatus status);
    List<Branch> findByType(BranchType type);
}
