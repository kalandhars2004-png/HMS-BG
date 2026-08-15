package com.phegondev.InventoryManagementSystem.variant;

import com.phegondev.InventoryManagementSystem.variant.Variant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {}
