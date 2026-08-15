package com.phegondev.InventoryManagementSystem.variant;

import com.phegondev.InventoryManagementSystem.variant.VariantDTO;
import com.phegondev.InventoryManagementSystem.common.Response;

public interface VariantService {
    Response createVariant(VariantDTO variantDTO);
    Response getAllVariants();
    Response getVariantById(Long id);
    Response updateVariant(Long id, VariantDTO variantDTO);
    Response deleteVariant(Long id);
}
