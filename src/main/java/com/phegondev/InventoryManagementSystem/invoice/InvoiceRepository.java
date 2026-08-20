package com.phegondev.InventoryManagementSystem.invoice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findAllByOrderByCreatedAtDesc();
    List<Invoice> findBySalesOrderId(Long salesOrderId);

    @Query("select max(i.id) from Invoice i")
    Long findMaxId();
}
