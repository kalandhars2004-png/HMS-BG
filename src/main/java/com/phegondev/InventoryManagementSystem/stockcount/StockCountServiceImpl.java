package com.phegondev.InventoryManagementSystem.stockcount;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.warehouse.Warehouse;
import com.phegondev.InventoryManagementSystem.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockCountServiceImpl implements StockCountService {

    private final StockCountRepository stockCountRepository;
    private final StockCountItemRepository stockCountItemRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final ModelMapper modelMapper;

    private String generateCountNumber() {
        Long maxId = stockCountRepository.findMaxId();
        long next = (maxId != null ? maxId : 0) + 1;
        String year = String.valueOf(LocalDateTime.now().getYear());
        return String.format("SC-%s-%04d", year, next);
    }

    @Override
    @Transactional
    public Response createStockCount(StockCountDTO stockCountDTO) {
        StockCount stockCount = StockCount.builder()
                .countNumber(generateCountNumber())
                .warehouseId(stockCountDTO.getWarehouseId())
                .status(stockCountDTO.getStatus() != null ? stockCountDTO.getStatus() : "DRAFT")
                .totalItems(0)
                .countedItems(0)
                .discrepancyCount(0)
                .notes(stockCountDTO.getNotes())
                .scheduledDate(stockCountDTO.getScheduledDate())
                .createdAt(LocalDateTime.now())
                .createdBy(stockCountDTO.getCreatedBy())
                .build();

        StockCount saved = stockCountRepository.save(stockCount);

        if ("DRAFT".equals(saved.getStatus())) {
            List<Product> products = productRepository.findAll();
            List<StockCountItem> items = products.stream()
                    .map(p -> StockCountItem.builder()
                            .stockCountId(saved.getId())
                            .productId(p.getId())
                            .expectedQuantity(p.getStockQuantity() != null ? p.getStockQuantity() : 0)
                            .actualQuantity(null)
                            .variance(0)
                            .status("PENDING")
                            .build())
                    .toList();
            stockCountItemRepository.saveAll(items);
            saved.setTotalItems(items.size());
            stockCountRepository.save(saved);
        }

        StockCountDTO result = buildDTO(saved);
        return Response.builder()
                .status(200)
                .message("Stock count created successfully")
                .stockCount(result)
                .build();
    }

    @Override
    public Response getAllStockCounts() {
        List<StockCount> stockCounts = stockCountRepository.findAllByOrderByCreatedAtDesc();
        List<StockCountDTO> dtos = stockCounts.stream().map(this::buildDTO).toList();
        return Response.builder()
                .status(200)
                .message("success")
                .stockCounts(dtos)
                .build();
    }

    @Override
    public Response getStockCountById(Long id) {
        StockCount stockCount = stockCountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Stock count not found"));
        StockCountDTO dto = buildDTO(stockCount);
        List<StockCountItem> items = stockCountItemRepository.findByStockCountId(id);
        List<StockCountItemDTO> itemDTOs = items.stream().map(this::buildItemDTO).toList();
        dto.setItems(itemDTOs);
        return Response.builder()
                .status(200)
                .message("success")
                .stockCount(dto)
                .build();
    }

    @Override
    public Response updateStockCountStatus(Long id, String status) {
        StockCount stockCount = stockCountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Stock count not found"));
        stockCount.setStatus(status);
        if ("CANCELLED".equals(status)) {
            stockCount.setCompletedDate(LocalDateTime.now());
        }
        stockCountRepository.save(stockCount);
        return Response.builder()
                .status(200)
                .message("Stock count status updated")
                .stockCount(buildDTO(stockCount))
                .build();
    }

    @Override
    public Response addItemToStockCount(Long stockCountId, StockCountItemDTO itemDTO) {
        stockCountRepository.findById(stockCountId)
                .orElseThrow(() -> new NotFoundException("Stock count not found"));
        StockCountItem item = StockCountItem.builder()
                .stockCountId(stockCountId)
                .productId(itemDTO.getProductId())
                .expectedQuantity(itemDTO.getExpectedQuantity())
                .actualQuantity(null)
                .variance(0)
                .status("PENDING")
                .notes(itemDTO.getNotes())
                .build();
        stockCountItemRepository.save(item);
        return Response.builder()
                .status(200)
                .message("Item added to stock count")
                .build();
    }

    @Override
    public Response updateItemCount(Long stockCountId, Long itemId, Integer actualQuantity) {
        StockCount stockCount = stockCountRepository.findById(stockCountId)
                .orElseThrow(() -> new NotFoundException("Stock count not found"));
        StockCountItem item = stockCountItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Stock count item not found"));

        if (!item.getStockCountId().equals(stockCountId)) {
            throw new IllegalArgumentException("Item does not belong to this stock count");
        }

        item.setActualQuantity(actualQuantity);
        int variance = actualQuantity != null && item.getExpectedQuantity() != null
                ? actualQuantity - item.getExpectedQuantity()
                : 0;
        item.setVariance(variance);
        item.setStatus(variance != 0 ? "DISCREPANCY" : "COUNTED");
        stockCountItemRepository.save(item);

        updateCountSummary(stockCount);

        return Response.builder()
                .status(200)
                .message("Item count updated")
                .build();
    }

    @Override
    @Transactional
    public Response completeStockCount(Long id) {
        StockCount stockCount = stockCountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Stock count not found"));
        List<StockCountItem> items = stockCountItemRepository.findByStockCountId(id);

        for (StockCountItem item : items) {
            int variance = item.getActualQuantity() != null && item.getExpectedQuantity() != null
                    ? item.getActualQuantity() - item.getExpectedQuantity()
                    : 0;
            item.setVariance(variance);
            if (item.getActualQuantity() == null) {
                item.setStatus("PENDING");
            } else {
                item.setStatus(variance != 0 ? "DISCREPANCY" : "COUNTED");
                // The entire purpose of a physical count: apply the variance to
                // the real stock. Previously this was computed and then discarded,
                // so staff counted shelves and the system kept its old numbers.
                if (variance != 0) {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new NotFoundException("Product not found for counted item"));
                    int corrected = (product.getStockQuantity() != null ? product.getStockQuantity() : 0) + variance;
                    product.setStockQuantity(Math.max(corrected, 0));
                    productRepository.save(product);
                }
            }
        }
        stockCountItemRepository.saveAll(items);

        stockCount.setStatus("COMPLETED");
        stockCount.setCompletedDate(LocalDateTime.now());
        updateCountSummary(stockCount);
        stockCountRepository.save(stockCount);

        return Response.builder()
                .status(200)
                .message("Stock count completed")
                .stockCount(buildDTO(stockCount))
                .build();
    }

    @Override
    public Response deleteStockCount(Long id) {
        stockCountRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Stock count not found"));
        stockCountItemRepository.findByStockCountId(id).forEach(stockCountItemRepository::delete);
        stockCountRepository.deleteById(id);
        return Response.builder()
                .status(200)
                .message("Stock count deleted successfully")
                .build();
    }

    private void updateCountSummary(StockCount stockCount) {
        List<StockCountItem> items = stockCountItemRepository.findByStockCountId(stockCount.getId());
        int totalItems = items.size();
        long counted = items.stream().filter(i -> i.getActualQuantity() != null).count();
        long discrepancies = items.stream().filter(i -> "DISCREPANCY".equals(i.getStatus())).count();
        stockCount.setTotalItems(totalItems);
        stockCount.setCountedItems((int) counted);
        stockCount.setDiscrepancyCount((int) discrepancies);
        stockCountRepository.save(stockCount);
    }

    private StockCountDTO buildDTO(StockCount sc) {
        StockCountDTO dto = modelMapper.map(sc, StockCountDTO.class);
        if (sc.getWarehouseId() != null) {
            Optional<Warehouse> wh = warehouseRepository.findById(sc.getWarehouseId());
            wh.ifPresent(w -> dto.setWarehouseName(w.getWarehouse()));
        }
        return dto;
    }

    private StockCountItemDTO buildItemDTO(StockCountItem item) {
        StockCountItemDTO dto = modelMapper.map(item, StockCountItemDTO.class);
        if (item.getProductId() != null) {
            Optional<Product> product = productRepository.findById(item.getProductId());
            product.ifPresent(p -> dto.setProductName(p.getName()));
        }
        return dto;
    }
}
