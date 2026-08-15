package com.phegondev.InventoryManagementSystem.eod;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyBusinessSummaryRepository extends JpaRepository<DailyBusinessSummary, Long> {
    Optional<DailyBusinessSummary> findByReportDate(LocalDate date);
}