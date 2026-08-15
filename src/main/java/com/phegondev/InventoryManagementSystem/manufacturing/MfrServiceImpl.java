package com.phegondev.InventoryManagementSystem.manufacturing;

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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfrServiceImpl implements MfrService {

    private final BillOfMaterialsRepository bomRepository;
    private final BOMItemRepository bomItemRepository;
    private final ProductionOrderRepository productionOrderRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    private final AtomicInteger mfrCounter = new AtomicInteger(0);

    private String generateOrderNumber() {
        int count = mfrCounter.incrementAndGet();
        return String.format("MFR-2026-%04d", count);
    }

    @Override
    @Transactional
    public Response addBOM(BillOfMaterialsDTO dto) {
        BillOfMaterials bom = BillOfMaterials.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .finishedProductId(dto.getFinishedProductId())
                .outputQuantity(dto.getOutputQuantity())
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        BillOfMaterials saved = bomRepository.save(bom);

        if (dto.getItems() != null) {
            for (BOMItemDTO itemDTO : dto.getItems()) {
                BOMItem item = BOMItem.builder()
                        .bomId(saved.getId())
                        .rawMaterialId(itemDTO.getRawMaterialId())
                        .quantity(itemDTO.getQuantity())
                        .unit(itemDTO.getUnit())
                        .build();
                bomItemRepository.save(item);
            }
        }

        BillOfMaterialsDTO resultDto = modelMapper.map(saved, BillOfMaterialsDTO.class);
        if (saved.getFinishedProductId() != null) {
            Product product = productRepository.findById(saved.getFinishedProductId()).orElse(null);
            if (product != null) resultDto.setFinishedProductName(product.getName());
        }
        resultDto.setItems(dto.getItems());

        return Response.builder()
                .status(200)
                .message("BOM created successfully")
                .billOfMaterials(resultDto)
                .build();
    }

    @Override
    public Response getAllBOMs() {
        List<BillOfMaterials> boms = bomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<BillOfMaterialsDTO> dtos = boms.stream().map(bom -> {
            BillOfMaterialsDTO dto = modelMapper.map(bom, BillOfMaterialsDTO.class);
            Product product = productRepository.findById(bom.getFinishedProductId()).orElse(null);
            if (product != null) dto.setFinishedProductName(product.getName());
            List<BOMItem> items = bomItemRepository.findByBomId(bom.getId());
            dto.setItems(items.stream().map(item -> {
                BOMItemDTO itemDTO = modelMapper.map(item, BOMItemDTO.class);
                Product raw = productRepository.findById(item.getRawMaterialId()).orElse(null);
                if (raw != null) itemDTO.setRawMaterialName(raw.getName());
                return itemDTO;
            }).toList());
            return dto;
        }).toList();

        return Response.builder()
                .status(200)
                .message("success")
                .boms(dtos)
                .build();
    }

    @Override
    public Response getBOMById(Long id) {
        BillOfMaterials bom = bomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("BOM Not Found"));

        BillOfMaterialsDTO dto = modelMapper.map(bom, BillOfMaterialsDTO.class);
        Product product = productRepository.findById(bom.getFinishedProductId()).orElse(null);
        if (product != null) dto.setFinishedProductName(product.getName());

        List<BOMItem> items = bomItemRepository.findByBomId(id);
        dto.setItems(items.stream().map(item -> {
            BOMItemDTO itemDTO = modelMapper.map(item, BOMItemDTO.class);
            Product raw = productRepository.findById(item.getRawMaterialId()).orElse(null);
            if (raw != null) itemDTO.setRawMaterialName(raw.getName());
            return itemDTO;
        }).toList());

        return Response.builder()
                .status(200)
                .message("success")
                .billOfMaterials(dto)
                .build();
    }

    @Override
    @Transactional
    public Response updateBOM(Long id, BillOfMaterialsDTO dto) {
        BillOfMaterials bom = bomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("BOM Not Found"));

        if (dto.getName() != null) bom.setName(dto.getName());
        if (dto.getDescription() != null) bom.setDescription(dto.getDescription());
        if (dto.getFinishedProductId() != null) bom.setFinishedProductId(dto.getFinishedProductId());
        if (dto.getOutputQuantity() != null) bom.setOutputQuantity(dto.getOutputQuantity());
        if (dto.getStatus() != null) bom.setStatus(dto.getStatus());

        bomRepository.save(bom);

        if (dto.getItems() != null) {
            bomItemRepository.deleteByBomId(id);
            for (BOMItemDTO itemDTO : dto.getItems()) {
                BOMItem item = BOMItem.builder()
                        .bomId(id)
                        .rawMaterialId(itemDTO.getRawMaterialId())
                        .quantity(itemDTO.getQuantity())
                        .unit(itemDTO.getUnit())
                        .build();
                bomItemRepository.save(item);
            }
        }

        return Response.builder()
                .status(200)
                .message("BOM updated successfully")
                .build();
    }

    @Override
    @Transactional
    public Response deleteBOM(Long id) {
        bomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("BOM Not Found"));

        bomItemRepository.deleteByBomId(id);
        bomRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("BOM deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public Response addProductionOrder(ProductionOrderDTO dto) {
        ProductionOrder order = ProductionOrder.builder()
                .orderNumber(generateOrderNumber())
                .bomId(dto.getBomId())
                .finishedProductId(dto.getFinishedProductId())
                .plannedQuantity(dto.getPlannedQuantity())
                .completedQuantity(0)
                .status("PLANNED")
                .createdAt(LocalDateTime.now())
                .createdBy(dto.getCreatedBy())
                .build();

        ProductionOrder savedOrder = productionOrderRepository.save(order);

        ProductionOrderDTO resultDto = modelMapper.map(savedOrder, ProductionOrderDTO.class);

        return Response.builder()
                .status(200)
                .message("Production order created successfully")
                .productionOrder(resultDto)
                .build();
    }

    @Override
    public Response getAllProductionOrders() {
        List<ProductionOrder> orders = productionOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<ProductionOrderDTO> dtos = orders.stream().map(order -> {
            ProductionOrderDTO dto = modelMapper.map(order, ProductionOrderDTO.class);
            Product product = productRepository.findById(order.getFinishedProductId()).orElse(null);
            if (product != null) dto.setFinishedProductName(product.getName());
            BillOfMaterials bom = bomRepository.findById(order.getBomId()).orElse(null);
            if (bom != null) dto.setBomName(bom.getName());
            return dto;
        }).toList();

        return Response.builder()
                .status(200)
                .message("success")
                .productionOrders(dtos)
                .build();
    }

    @Override
    public Response getProductionOrderById(Long id) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Production Order Not Found"));

        ProductionOrderDTO dto = modelMapper.map(order, ProductionOrderDTO.class);
        Product product = productRepository.findById(order.getFinishedProductId()).orElse(null);
        if (product != null) dto.setFinishedProductName(product.getName());
        BillOfMaterials bom = bomRepository.findById(order.getBomId()).orElse(null);
        if (bom != null) dto.setBomName(bom.getName());

        return Response.builder()
                .status(200)
                .message("success")
                .productionOrder(dto)
                .build();
    }

    @Override
    public Response updateProductionOrderStatus(Long id, String status) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Production Order Not Found"));

        order.setStatus(status);
        productionOrderRepository.save(order);

        return Response.builder()
                .status(200)
                .message("Production order status updated")
                .build();
    }

    @Override
    @Transactional
    public Response startProduction(Long id) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Production Order Not Found"));

        order.setStatus("IN_PROGRESS");
        order.setStartDate(LocalDateTime.now());
        productionOrderRepository.save(order);

        return Response.builder()
                .status(200)
                .message("Production started")
                .build();
    }

    @Override
    @Transactional
    public Response completeProduction(Long id) {
        ProductionOrder order = productionOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Production Order Not Found"));

        BillOfMaterials bom = bomRepository.findById(order.getBomId())
                .orElseThrow(() -> new NotFoundException("BOM Not Found"));

        List<BOMItem> bomItems = bomItemRepository.findByBomId(bom.getId());

        for (BOMItem bomItem : bomItems) {
            Product rawMaterial = productRepository.findById(bomItem.getRawMaterialId())
                    .orElseThrow(() -> new NotFoundException("Raw material not found"));

            int requiredQty = bomItem.getQuantity() * order.getPlannedQuantity();
            if (rawMaterial.getStockQuantity() < requiredQty) {
                throw new IllegalStateException("Insufficient raw material: " + rawMaterial.getName());
            }
            rawMaterial.setStockQuantity(rawMaterial.getStockQuantity() - requiredQty);
            productRepository.save(rawMaterial);
        }

        Product finishedProduct = productRepository.findById(bom.getFinishedProductId())
                .orElseThrow(() -> new NotFoundException("Finished product not found"));

        int outputQty = bom.getOutputQuantity() * order.getPlannedQuantity();
        finishedProduct.setStockQuantity(finishedProduct.getStockQuantity() + outputQty);
        productRepository.save(finishedProduct);

        order.setCompletedQuantity(order.getPlannedQuantity());
        order.setStatus("COMPLETED");
        order.setEndDate(LocalDateTime.now());
        productionOrderRepository.save(order);

        return Response.builder()
                .status(200)
                .message("Production completed successfully")
                .build();
    }
}
