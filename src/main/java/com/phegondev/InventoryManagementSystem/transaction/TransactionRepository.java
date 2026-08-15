package com.phegondev.InventoryManagementSystem.transaction;

import com.phegondev.InventoryManagementSystem.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    @Query("SELECT t FROM Transaction t " +
            "WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    List<Transaction> findAllByMonthAndYear(@Param("month") int month, @Param("year") int year);


    //we are searching these field; Transaction's description, note, status, Product's name, sku
    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.product p " +
            "WHERE (:searchText IS NULL OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(t.status) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :searchText, '%')))")
    Page<Transaction> searchTransactions(@Param("searchText") String searchText, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.transactionType = :type AND t.createdAt BETWEEN :start AND :end")
    List<Transaction> findByTransactionTypeAndCreatedAtBetween(
            @Param("type") com.phegondev.InventoryManagementSystem.enums.TransactionType type,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

}
