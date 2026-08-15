package com.phegondev.InventoryManagementSystem.equipment;

import com.phegondev.InventoryManagementSystem.equipment.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {}
