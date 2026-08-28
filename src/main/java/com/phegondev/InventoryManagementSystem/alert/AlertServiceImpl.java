package com.phegondev.InventoryManagementSystem.alert;

import com.phegondev.InventoryManagementSystem.batch.Batch;
import com.phegondev.InventoryManagementSystem.batch.BatchRepository;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response checkAndCreateAlerts() {
        int created = 0;

        // Only products actually at/below the threshold are fetched; previously the
        // whole catalogue was loaded and filtered in Java.
        List<Product> lowStockProducts = productRepository.findByStockQuantityLessThanEqual(10);

        // Unread LOW_STOCK ids fetched once. The old dedup streamed the entire
        // unread-alert list once per product (O(products x alerts) with a query
        // hidden inside).
        Set<Long> existingLowStockIds = alertRepository
                .findByReadFalseOrderByCreatedAtDesc().stream()
                .filter(a -> "LOW_STOCK".equals(a.getType()))
                .map(Alert::getRelatedEntityId)
                .collect(Collectors.toSet());

        for (Product product : lowStockProducts) {
            if (product.getStockQuantity() == null || existingLowStockIds.contains(product.getId())) {
                continue;
            }
            Alert alert = Alert.builder()
                .type("LOW_STOCK")
                .severity(product.getStockQuantity() <= 0 ? "CRITICAL" : "WARNING")
                .title("Low Stock: " + product.getName())
                .message("Product " + product.getName() + " has only " + product.getStockQuantity() + " units left")
                .relatedEntityId(product.getId())
                .relatedEntityType("PRODUCT")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
            alertRepository.save(alert);
            created++;
        }

        // Expiring-soon batches come from an indexed range query instead of loading
        // every batch row ever recorded.
        List<Batch> expiringBatches = batchRepository.findByExpiryDateBetween(
                LocalDateTime.now(), LocalDateTime.now().plusDays(30));

        for (Batch batch : expiringBatches) {
            Alert alert = Alert.builder()
                .type("EXPIRING_BATCH")
                .severity("WARNING")
                .title("Batch Expiring Soon")
                .message("Batch " + batch.getBatchNo() + " expires on " + batch.getExpiryDate().toLocalDate())
                .relatedEntityId(batch.getId())
                .relatedEntityType("BATCH")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
            alertRepository.save(alert);
            created++;
        }

        return Response.builder()
            .status(200)
            .message(created + " new alerts created")
            .build();
    }

    @Override
    public Response getUnreadAlerts() {
        List<Alert> alerts = alertRepository.findByReadFalseOrderByCreatedAtDesc();
        List<AlertDTO> dtos = alerts.stream().map(a -> modelMapper.map(a, AlertDTO.class)).toList();
        return Response.builder().status(200).alerts(dtos).build();
    }

    @Override
    public Response getAllAlerts() {
        List<Alert> alerts = alertRepository.findAllByOrderByCreatedAtDesc();
        List<AlertDTO> dtos = alerts.stream().map(a -> modelMapper.map(a, AlertDTO.class)).toList();
        return Response.builder().status(200).alerts(dtos).build();
    }

    @Override
    public Response markAsRead(Long id) {
        Alert alert = alertRepository.findById(id).orElse(null);
        if (alert != null) {
            alert.setRead(true);
            alertRepository.save(alert);
        }
        return Response.builder().status(200).message("Alert marked as read").build();
    }

    @Override
    public Response markAllAsRead() {
        List<Alert> unread = alertRepository.findByReadFalseOrderByCreatedAtDesc();
        unread.forEach(a -> a.setRead(true));
        alertRepository.saveAll(unread);
        return Response.builder().status(200).message("All alerts marked as read").build();
    }

    @Override
    public Response getUnreadCount() {
        long count = alertRepository.countByReadFalse();
        return Response.builder().status(200).message(String.valueOf(count)).build();
    }
}