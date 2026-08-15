package com.phegondev.InventoryManagementSystem.brand;

import com.phegondev.InventoryManagementSystem.brand.BrandDTO;
import com.phegondev.InventoryManagementSystem.common.Response;

public interface BrandService {
    Response createBrand(BrandDTO brandDTO);
    Response getAllBrands();
    Response getBrandById(Long id);
    Response updateBrand(Long id, BrandDTO brandDTO);
    Response deleteBrand(Long id);
}
