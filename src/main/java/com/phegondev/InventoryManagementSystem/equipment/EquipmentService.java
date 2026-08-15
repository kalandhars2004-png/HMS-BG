package com.phegondev.InventoryManagementSystem.equipment;

import com.phegondev.InventoryManagementSystem.equipment.EquipmentDTO;
import com.phegondev.InventoryManagementSystem.common.Response;

public interface EquipmentService {
    Response createEquipment(EquipmentDTO equipmentDTO);
    Response getAllEquipments();
    Response getEquipmentById(Long id);
    Response updateEquipment(Long id, EquipmentDTO equipmentDTO);
    Response deleteEquipment(Long id);
}
