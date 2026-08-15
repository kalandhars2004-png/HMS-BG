package com.phegondev.InventoryManagementSystem.unit;

import com.phegondev.InventoryManagementSystem.unit.UnitDTO;
import com.phegondev.InventoryManagementSystem.common.Response;

public interface UnitService {
    Response createUnit(UnitDTO unitDTO);
    Response getAllUnits();
    Response filterUnits(UnitFilter filter);
    Response getUnitById(Long id);
    Response updateUnit(Long id, UnitDTO unitDTO);
    Response deleteUnit(Long id);
}
