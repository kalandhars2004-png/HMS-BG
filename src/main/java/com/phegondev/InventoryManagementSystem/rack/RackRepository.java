package com.phegondev.InventoryManagementSystem.rack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RackRepository extends JpaRepository<Rack, Long> {
    Optional<Rack> findByCodeIgnoreCase(String code);
}
