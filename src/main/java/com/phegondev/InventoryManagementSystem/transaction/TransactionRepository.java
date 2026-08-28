package com.phegondev.InventoryManagementSystem.transaction;

import com.phegondev.InventoryManagementSystem.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {


    @Query("SELECT t FROM Transaction t " +
            "WHERE YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
    List<Transaction> findAllByMonthAndYear(@Param("month") int month, @Param("year") int year);


    /**
     * Searches description, status, and the product's name and sku.
     *
     * Two casts are load-bearing on PostgreSQL:
     *  - CAST(:searchText AS string) — without it, a null argument leaves the bind
     *    parameter untyped and Postgres infers bytea, so the whole statement fails
     *    with "function lower(bytea) does not exist". That made the no-search case
     *    (GET /api/transactions/all) return 500 for every caller.
     *  - CAST(t.status AS string) — status is @Enumerated(STRING); LOWER() cannot be
     *    applied to an enum path directly.
     */
    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.product p " +
            "WHERE (CAST(:searchText AS string) IS NULL OR CAST(:searchText AS string) = '' OR " +
            "LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', CAST(:searchText AS string), '%')) OR " +
            "LOWER(CAST(t.status AS string)) LIKE LOWER(CONCAT('%', CAST(:searchText AS string), '%')) OR " +
            "LOWER(COALESCE(p.name, '')) LIKE LOWER(CONCAT('%', CAST(:searchText AS string), '%')) OR " +
            "LOWER(COALESCE(p.sku, '')) LIKE LOWER(CONCAT('%', CAST(:searchText AS string), '%')))")
    Page<Transaction> searchTransactions(@Param("searchText") String searchText, Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.transactionType = :type AND t.createdAt BETWEEN :start AND :end")
    List<Transaction> findByTransactionTypeAndCreatedAtBetween(
            @Param("type") com.phegondev.InventoryManagementSystem.enums.TransactionType type,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

    /** Sum of total prices for a transaction type in a window — EOD purchases total. */
    @Query("SELECT COALESCE(SUM(t.totalPrice), 0) FROM Transaction t " +
            "WHERE t.transactionType = :type AND t.createdAt BETWEEN :start AND :end")
    BigDecimal sumTotalPriceByTypeAndCreatedAtBetween(
            @Param("type") com.phegondev.InventoryManagementSystem.enums.TransactionType type,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end);

}
