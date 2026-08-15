package com.phegondev.InventoryManagementSystem.supplier;

import com.phegondev.InventoryManagementSystem.supplier.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
