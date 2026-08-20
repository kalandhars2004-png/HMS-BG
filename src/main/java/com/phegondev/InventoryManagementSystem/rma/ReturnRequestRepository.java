package com.phegondev.InventoryManagementSystem.rma;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Long> {
    List<ReturnRequest> findAllByOrderByCreatedAtDesc();

    @Query("select max(r.id) from ReturnRequest r")
    Long findMaxId();
}
