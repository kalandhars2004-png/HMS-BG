package com.phegondev.InventoryManagementSystem.kitting;

import com.phegondev.InventoryManagementSystem.common.Response;

public interface KitService {

    Response addKit(KitDTO kitDTO);

    Response getAllKits();

    Response getKitById(Long id);

    Response updateKit(Long id, KitDTO kitDTO);

    Response deleteKit(Long id);

    Response assembleKit(Long kitId, Integer quantity);

    Response disassembleKit(Long kitId, Integer quantity);
}
