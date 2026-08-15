package com.phegondev.InventoryManagementSystem.product;

import com.phegondev.InventoryManagementSystem.product.ProductDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.product.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;


    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> saveProduct(
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam("name") String  name,
            @RequestParam("sku") String  sku,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer  stockQuantity,
            @RequestParam("categoryId") Long  categoryId,
            @RequestParam(value = "description", required = false) String  description,
            @RequestParam(value = "genericName", required = false) String genericName,
            @RequestParam(value = "barcode", required = false) String barcode,
            @RequestParam(value = "mrp", required = false) BigDecimal mrp,
            @RequestParam(value = "purchasePrice", required = false) BigDecimal purchasePrice,
            @RequestParam(value = "taxPercentage", required = false) BigDecimal taxPercentage,
            @RequestParam(value = "discountPercentage", required = false) BigDecimal discountPercentage,
            @RequestParam(value = "lowStockQuantity", required = false) Integer lowStockQuantity,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "manufacturingDate", required = false) String manufacturingDate,
            @RequestParam(value = "prescriptionRequired", required = false) Boolean prescriptionRequired,
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "unitId", required = false) Long unitId,
            @RequestParam(value = "variantId", required = false) Long variantId,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId
            ) {
        ProductDTO productDTO = new ProductDTO();

        productDTO.setName(name);
        productDTO.setSku(sku);
        productDTO.setPrice(price);
        productDTO.setStockQuantity(stockQuantity);
        productDTO.setCategoryId(categoryId);
        productDTO.setDescription(description);
        productDTO.setGenericName(genericName);
        productDTO.setBarcode(barcode);
        productDTO.setMrp(mrp);
        productDTO.setPurchasePrice(purchasePrice);
        productDTO.setTaxPercentage(taxPercentage);
        productDTO.setDiscountPercentage(discountPercentage);
        productDTO.setLowStockQuantity(lowStockQuantity);
        productDTO.setExpiryDate(expiryDate != null ? LocalDateTime.parse(expiryDate) : null);
        productDTO.setManufacturingDate(manufacturingDate != null ? LocalDateTime.parse(manufacturingDate) : null);
        productDTO.setPrescriptionRequired(prescriptionRequired);
        productDTO.setBrandId(brandId);
        productDTO.setUnitId(unitId);
        productDTO.setVariantId(variantId);
        productDTO.setWarehouseId(warehouseId);

        return ResponseEntity.ok(productService.saveProduct(productDTO, imageFile));
    }
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> updateProduct(
            @RequestParam(value = "imageFile", required=false) MultipartFile imageFile,
            @RequestParam(value = "name",required = false) String  name,
            @RequestParam(value = "sku",required = false) String  sku,
            @RequestParam(value = "price",required = false) BigDecimal price,
            @RequestParam(value = "stockQuantity",required = false) Integer  stockQuantity,
            @RequestParam(value = "productId",required = true) Long  productId,
            @RequestParam(value = "categoryId",required = false) Long  categoryId,
            @RequestParam(value = "description", required = false) String  description,
            @RequestParam(value = "genericName", required = false) String genericName,
            @RequestParam(value = "barcode", required = false) String barcode,
            @RequestParam(value = "mrp", required = false) BigDecimal mrp,
            @RequestParam(value = "purchasePrice", required = false) BigDecimal purchasePrice,
            @RequestParam(value = "taxPercentage", required = false) BigDecimal taxPercentage,
            @RequestParam(value = "discountPercentage", required = false) BigDecimal discountPercentage,
            @RequestParam(value = "lowStockQuantity", required = false) Integer lowStockQuantity,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "manufacturingDate", required = false) String manufacturingDate,
            @RequestParam(value = "prescriptionRequired", required = false) Boolean prescriptionRequired,
            @RequestParam(value = "brandId", required = false) Long brandId,
            @RequestParam(value = "unitId", required = false) Long unitId,
            @RequestParam(value = "variantId", required = false) Long variantId,
            @RequestParam(value = "warehouseId", required = false) Long warehouseId
    ) {
        ProductDTO productDTO = new ProductDTO();

        productDTO.setName(name);
        productDTO.setSku(sku);
        productDTO.setPrice(price);
        productDTO.setStockQuantity(stockQuantity);
        productDTO.setCategoryId(categoryId);
        productDTO.setProductId(productId);
        productDTO.setDescription(description);
        productDTO.setGenericName(genericName);
        productDTO.setBarcode(barcode);
        productDTO.setMrp(mrp);
        productDTO.setPurchasePrice(purchasePrice);
        productDTO.setTaxPercentage(taxPercentage);
        productDTO.setDiscountPercentage(discountPercentage);
        productDTO.setLowStockQuantity(lowStockQuantity);
        productDTO.setExpiryDate(expiryDate != null ? LocalDateTime.parse(expiryDate) : null);
        productDTO.setManufacturingDate(manufacturingDate != null ? LocalDateTime.parse(manufacturingDate) : null);
        productDTO.setPrescriptionRequired(prescriptionRequired);
        productDTO.setBrandId(brandId);
        productDTO.setUnitId(unitId);
        productDTO.setVariantId(variantId);
        productDTO.setWarehouseId(warehouseId);

        return ResponseEntity.ok(productService.updateProduct(productDTO, imageFile));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Response> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }


    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }


}
