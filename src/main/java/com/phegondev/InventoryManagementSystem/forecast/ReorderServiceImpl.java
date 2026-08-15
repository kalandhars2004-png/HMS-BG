package com.phegondev.InventoryManagementSystem.forecast;

import com.phegondev.InventoryManagementSystem.enums.TransactionType;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.transaction.Transaction;
import com.phegondev.InventoryManagementSystem.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReorderServiceImpl implements ReorderService {

    private final ReorderPointRepository reorderPointRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public ReorderPointDTO setReorderPoint(ReorderPointDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ReorderPoint point = reorderPointRepository.findByProductId(dto.getProductId())
                .orElse(new ReorderPoint());

        point.setProductId(dto.getProductId());
        point.setMinStockLevel(dto.getMinStockLevel());
        point.setMaxStockLevel(dto.getMaxStockLevel());
        point.setReorderPoint(dto.getReorderPoint());
        point.setReorderQuantity(dto.getReorderQuantity());
        point.setStatus(dto.getStatus() != null ? dto.getStatus() : "ACTIVE");

        LocalDateTime now = LocalDateTime.now();
        if (point.getId() == null) {
            point.setCreatedAt(now);
        }
        point.setUpdatedAt(now);

        ReorderPoint saved = reorderPointRepository.save(point);
        return toDTO(saved, product);
    }

    @Override
    public ReorderPointDTO getReorderPoint(Long productId) {
        ReorderPoint point = reorderPointRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Reorder point not found for product: " + productId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toDTO(point, product);
    }

    @Override
    public List<ReorderPointDTO> getAllReorderPoints() {
        List<ReorderPoint> points = reorderPointRepository.findAll();
        Map<Long, Product> productMap = productRepository.findAll()
                .stream().collect(Collectors.toMap(Product::getId, p -> p));

        return points.stream()
                .map(p -> toDTO(p, productMap.get(p.getProductId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ReorderPointDTO> getNeedsReorder() {
        return getAllReorderPoints().stream()
                .filter(dto -> dto.isNeedsReorder())
                .collect(Collectors.toList());
    }

    @Override
    public List<ForecastResult> getForecast() {
        List<Product> products = productRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime ninetyDaysAgo = now.minusDays(90);

        List<Transaction> transactions = transactionRepository
                .findByTransactionTypeAndCreatedAtBetween(TransactionType.SALE, ninetyDaysAgo, now);

        Map<Long, List<Transaction>> txByProduct = transactions.stream()
                .filter(t -> t.getProduct() != null)
                .collect(Collectors.groupingBy(t -> t.getProduct().getId()));

        List<ForecastResult> results = new ArrayList<>();

        for (Product product : products) {
            List<Transaction> productTx = txByProduct.getOrDefault(product.getId(), Collections.emptyList());

            int totalSold = productTx.stream()
                    .mapToInt(t -> t.getTotalProducts() != null ? t.getTotalProducts() : 0)
                    .sum();

            double avgMonthlySales = totalSold / 3.0;
            int currentStock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;

            int daysUntilOutOfStock;
            String recommendation;
            int suggestedOrderQuantity;

            if (avgMonthlySales <= 0) {
                daysUntilOutOfStock = 999;
                recommendation = "NO_ACTION";
                suggestedOrderQuantity = 0;
            } else {
                double dailyRate = avgMonthlySales / 30.0;
                daysUntilOutOfStock = (int) Math.floor(currentStock / dailyRate);
                if (daysUntilOutOfStock < 7) {
                    recommendation = "ORDER";
                } else if (daysUntilOutOfStock > 90) {
                    recommendation = "OVERSTOCKED";
                } else {
                    recommendation = "NO_ACTION";
                }
                suggestedOrderQuantity = Math.max(0, (int) Math.ceil((avgMonthlySales * 2) - currentStock));
            }

            results.add(ForecastResult.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .currentStock(currentStock)
                    .averageMonthlySales((int) Math.round(avgMonthlySales))
                    .daysUntilOutOfStock(daysUntilOutOfStock)
                    .recommendation(recommendation)
                    .suggestedOrderQuantity(suggestedOrderQuantity)
                    .calculatedAt(now)
                    .build());
        }

        return results;
    }

    @Override
    public void delete(Long id) {
        reorderPointRepository.deleteById(id);
    }

    private ReorderPointDTO toDTO(ReorderPoint point, Product product) {
        boolean needsReorder = product.getStockQuantity() != null
                && point.getReorderPoint() != null
                && product.getStockQuantity() <= point.getReorderPoint();

        return ReorderPointDTO.builder()
                .id(point.getId())
                .productId(point.getProductId())
                .productName(product != null ? product.getName() : null)
                .currentStock(product != null ? product.getStockQuantity() : null)
                .minStockLevel(point.getMinStockLevel())
                .maxStockLevel(point.getMaxStockLevel())
                .reorderPoint(point.getReorderPoint())
                .reorderQuantity(point.getReorderQuantity())
                .status(point.getStatus())
                .needsReorder(needsReorder)
                .build();
    }
}
