package com.phegondev.InventoryManagementSystem.unit;

import com.phegondev.InventoryManagementSystem.unit.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long>, JpaSpecificationExecutor<Unit> {
    Optional<Unit> findByNameIgnoreCase(String name);
    Optional<Unit> findByShortNameIgnoreCase(String shortName);
}
