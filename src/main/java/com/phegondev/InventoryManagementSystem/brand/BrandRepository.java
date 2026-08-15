package com.phegondev.InventoryManagementSystem.brand;

import com.phegondev.InventoryManagementSystem.brand.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {}
