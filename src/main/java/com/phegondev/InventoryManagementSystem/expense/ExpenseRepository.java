package com.phegondev.InventoryManagementSystem.expense;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByBranchIdOrderByDateDesc(Long branchId);
    List<Expense> findByOrganizationIdOrderByDateDesc(Long orgId);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.branchId = :branchId AND e.date BETWEEN :from AND :to")
    BigDecimal sumByBranchAndDate(Long branchId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(e.amount),0) FROM Expense e WHERE e.organizationId = :orgId AND e.date BETWEEN :from AND :to")
    BigDecimal sumByOrgAndDate(Long orgId, LocalDateTime from, LocalDateTime to);
}
