package com.phegondev.InventoryManagementSystem.pos;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface POSSessionRepository extends JpaRepository<POSSession, Long> {
    List<POSSession> findAllByOrderByOpenedAtDesc();
    List<POSSession> findByStatus(String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from POSSession s where s.id = :id")
    Optional<POSSession> findByIdWithLock(@Param("id") Long id);
}
