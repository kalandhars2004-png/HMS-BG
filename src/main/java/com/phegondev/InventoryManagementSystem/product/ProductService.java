package com.phegondev.InventoryManagementSystem.product;

import com.phegondev.InventoryManagementSystem.product.ProductDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import org.springframework.web.multipart.MultipartFile;

public interface ProductService {
    Response saveProduct(ProductDTO productDTO, MultipartFile imageFile);
    Response updateProduct(ProductDTO productDTO, MultipartFile imageFile);

    /**
     * Legacy full list when page/size are null; paged result when both provided.
     * Keeps existing callers working while letting the UI opt into pagination as
     * the catalogue grows.
     */
    default Response getAllProducts() {
        return getAllProducts(null, null);
    }

    Response getAllProducts(Integer page, Integer size);
    Response getProductById(Long id);
    Response deleteProduct(Long id);
}
