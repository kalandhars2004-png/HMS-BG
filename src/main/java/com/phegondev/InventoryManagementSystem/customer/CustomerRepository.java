package com.phegondev.InventoryManagementSystem.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByPhone(String phone);
    Optional<Customer> findByName(String name);
    List<Customer> findByNameContainingIgnoreCaseOrPhoneContaining(String name, String phone);
    List<Customer> findByBranchId(Long branchId);
    List<Customer> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByBranchId(Long branchId);
}