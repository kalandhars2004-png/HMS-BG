package com.phegondev.InventoryManagementSystem.salesorder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    List<SalesOrder> findAllByOrderByCreatedAtDesc();
    List<SalesOrder> findByBranchIdOrderByCreatedAtDesc(Long branchId);

    @Query("select max(s.id) from SalesOrder s")
    Long findMaxId();
}
