package com.phegondev.InventoryManagementSystem.barcode;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BarcodeScanServiceImpl implements BarcodeScanService {

    private final BarcodeScanRepository barcodeScanRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response scanBarcode(BarcodeScanRequest request) {
        Product product = productRepository.findByBarcode(request.getBarcode())
                .stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Product not found with barcode: " + request.getBarcode()));

        BarcodeScan scan = BarcodeScan.builder()
                .barcode(request.getBarcode())
                .productId(product.getId())
                .action(request.getAction())
                .quantity(request.getQuantity())
                .location(request.getLocation())
                .createdAt(LocalDateTime.now())
                .build();

        barcodeScanRepository.save(scan);

        BarcodeScanDTO dto = modelMapper.map(scan, BarcodeScanDTO.class);
        dto.setProductName(product.getName());

        return Response.builder()
                .status(200)
                .message("Scan recorded successfully")
                .barcodeScan(dto)
                .build();
    }

    @Override
    public Response getByBarcode(String barcode) {
        List<BarcodeScan> scans = barcodeScanRepository.findByBarcode(barcode);
        List<BarcodeScanDTO> dtos = modelMapper.map(scans, new TypeToken<List<BarcodeScanDTO>>() {}.getType());
        return Response.builder()
                .status(200)
                .message("success")
                .barcodeScans(dtos)
                .build();
    }

    @Override
    public Response getAllScans() {
        List<BarcodeScan> scans = barcodeScanRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<BarcodeScanDTO> dtos = modelMapper.map(scans, new TypeToken<List<BarcodeScanDTO>>() {}.getType());

        for (int i = 0; i < scans.size(); i++) {
            Product product = productRepository.findById(scans.get(i).getProductId()).orElse(null);
            if (product != null) {
                dtos.get(i).setProductName(product.getName());
            }
        }

        return Response.builder()
                .status(200)
                .message("success")
                .barcodeScans(dtos)
                .build();
    }

    @Override
    public Response lookupProduct(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .stream().findFirst()
                .orElseThrow(() -> new NotFoundException("Product not found with barcode: " + barcode));

        com.phegondev.InventoryManagementSystem.product.ProductDTO dto = modelMapper.map(product, com.phegondev.InventoryManagementSystem.product.ProductDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .product(dto)
                .build();
    }
}
