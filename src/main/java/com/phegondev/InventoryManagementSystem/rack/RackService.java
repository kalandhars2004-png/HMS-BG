package com.phegondev.InventoryManagementSystem.rack;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface RackService {
    Response createRack(RackDTO rackDTO);
    Response getAllRacks();
    Response getRackById(Long id);
    Response updateRack(Long id, RackDTO rackDTO);
    Response deleteRack(Long id);
}
