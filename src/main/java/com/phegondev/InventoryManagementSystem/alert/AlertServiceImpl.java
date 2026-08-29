package com.phegondev.InventoryManagementSystem.alert;

import com.phegondev.InventoryManagementSystem.batch.Batch;
import com.phegondev.InventoryManagementSystem.batch.BatchRepository;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;

    // ================= Centralized creation with deduplication =================
    @Transactional
    public Alert createNotification(String type, String severity, String title, String message,
                                    Long entityId, String entityType, Long branchId, Long organizationId,
                                    String metadata) {
        // Deduplication for state-based alerts: one unresolved alert per (type, entity, branch)
        if (type.equals("LOW_STOCK") || type.equals("OUT_OF_STOCK") || type.equals("EXPIRING_SOON") || type.equals("EXPIRED")) {
            Optional<Alert> existing = alertRepository.findFirstByTypeAndRelatedEntityIdAndBranchIdAndIsResolvedFalse(type, entityId, branchId);
            if (existing.isPresent()) {
                log.debug("Dedup: skip {} for entity {} branch {}", type, entityId, branchId);
                return existing.get();
            }
        }
        // For event-based (SALE, PURCHASE, PAYMENT), allow one per event — dedup by entityId+type globally would block legitimate repeats
        // So we don't dedup there, or we check read=false + same entity would be duplicate only if within 1 min (simple)
        Alert alert = Alert.builder()
                .type(type)
                .severity(severity)
                .title(title)
                .message(message)
                .relatedEntityId(entityId)
                .relatedEntityType(entityType)
                .branchId(branchId)
                .organizationId(organizationId != null ? organizationId : 1L)
                .read(false)
                .isResolved(false)
                .metadata(metadata)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        // Try to enrich with TenantContext user if available
        try {
            var tenant = TenantContext.get();
            if (tenant != null && tenant.userId() != null) alert.setUserId(tenant.userId());
        } catch (Exception ignored) {}
        return alertRepository.save(alert);
    }

    // Helper for product stock events — called from ProductService or scheduler
    public void checkProductStock(Product product) {
        if (product == null || product.getStockQuantity() == null) return;
        Long branchId = product.getBranchId();
        Long orgId = product.getOrganizationId();
        int qty = product.getStockQuantity();
        int threshold = product.getLowStockQuantity() != null ? product.getLowStockQuantity() : 10;

        if (qty == 0) {
            // OUT_OF_STOCK — critical, and resolve any LOW_STOCK for same product
            resolveAlerts(product.getId(), "LOW_STOCK", branchId);
            createNotification("OUT_OF_STOCK", "CRITICAL", "Out of Stock: " + product.getName(),
                    product.getName() + " is out of stock.", product.getId(), "PRODUCT", branchId, orgId,
                    "{\"currentStock\":0,\"threshold\":" + threshold + "}");
        } else if (qty <= threshold) {
            // LOW_STOCK — warning, but if already OUT_OF_STOCK unresolved, don't duplicate
            Optional<Alert> out = alertRepository.findFirstByTypeAndRelatedEntityIdAndBranchIdAndIsResolvedFalse("OUT_OF_STOCK", product.getId(), branchId);
            if (out.isPresent()) return;
            createNotification("LOW_STOCK", "WARNING", "Low Stock: " + product.getName(),
                    product.getName() + " is running low (" + qty + " units, min " + threshold + ").", product.getId(), "PRODUCT", branchId, orgId,
                    "{\"currentStock\":" + qty + ",\"threshold\":" + threshold + "}");
            // If stock recovered, the OUT_OF_STOCK will be resolved on next check (see resolve)
        } else {
            // Stock healthy — resolve any existing low/out alerts
            resolveAlerts(product.getId(), "LOW_STOCK", branchId);
            resolveAlerts(product.getId(), "OUT_OF_STOCK", branchId);
        }
    }

    private void resolveAlerts(Long entityId, String type, Long branchId) {
        alertRepository.findFirstByTypeAndRelatedEntityIdAndBranchIdAndIsResolvedFalse(type, entityId, branchId)
                .ifPresent(a -> {
                    a.setIsResolved(true);
                    a.setResolvedAt(LocalDateTime.now());
                    alertRepository.save(a);
                });
    }

    // Batch expiry checks
    public void checkBatchExpiry(Batch batch) {
        if (batch == null || batch.getExpiryDate() == null) return;
        long days = ChronoUnit.DAYS.between(LocalDateTime.now(), batch.getExpiryDate());
        Long branchId = batch.getBranchId();
        Long orgId = batch.getOrganizationId();
        if (days < 0) {
            createNotification("EXPIRED", "CRITICAL", "Expired: " + batch.getBatchNo(),
                    "Batch " + batch.getBatchNo() + " expired on " + batch.getExpiryDate().toLocalDate(), batch.getId(), "BATCH", branchId, orgId,
                    "{\"expiryDate\":\"" + batch.getExpiryDate() + "\",\"daysRemaining\":" + days + "}");
            // Resolve EXPIRING_SOON if now expired
            resolveAlerts(batch.getId(), "EXPIRING_SOON", branchId);
        } else if (days <= 30) {
            // Dedup will prevent duplicate EXPIRING_SOON
            createNotification("EXPIRING_SOON", "WARNING", "Expiring Soon: " + batch.getBatchNo(),
                    "Batch " + batch.getBatchNo() + " expires in " + days + " days.", batch.getId(), "BATCH", branchId, orgId,
                    "{\"expiryDate\":\"" + batch.getExpiryDate() + "\",\"daysRemaining\":" + days + "}");
        } else {
            // Far future — resolve any prior expiring
            resolveAlerts(batch.getId(), "EXPIRING_SOON", branchId);
            resolveAlerts(batch.getId(), "EXPIRED", branchId);
        }
    }

    // Event helpers — called from services
    public void notifySaleCreated(Long saleId, String customer, java.math.BigDecimal amount, Long branchId) {
        createNotification("SALE_CREATED", "SUCCESS", "New Sale — " + formatAmount(amount),
                "Sale " + saleId + (customer != null ? " for " + customer : "") + " — " + formatAmount(amount), saleId, "SALE", branchId, 1L,
                "{\"saleId\":" + saleId + ",\"amount\":" + amount + "}");
    }
    public void notifyPurchaseCreated(Long purchaseId, java.math.BigDecimal amount, Long branchId) {
        createNotification("PURCHASE_CREATED", "INFO", "Purchase Recorded — " + formatAmount(amount),
                "Purchase " + purchaseId + " — " + formatAmount(amount), purchaseId, "PURCHASE", branchId, 1L,
                "{\"purchaseId\":" + purchaseId + ",\"amount\":" + amount + "}");
    }
    public void notifyPayment(String status, Long txnId, java.math.BigDecimal amount, String method, Long branchId) {
        String type = "PAYMENT_SUCCESS";
        String sev = "SUCCESS";
        if ("FAILED".equalsIgnoreCase(status)) { type = "PAYMENT_FAILED"; sev = "ERROR"; }
        else if ("PENDING".equalsIgnoreCase(status)) { type = "PAYMENT_PENDING"; sev = "INFO"; }
        createNotification(type, sev, "Payment " + status + " — " + formatAmount(amount),
                "Payment of " + formatAmount(amount) + " " + status.toLowerCase() + (method != null ? " via " + method : ""), txnId, "PAYMENT", branchId, 1L,
                "{\"txnId\":" + txnId + ",\"amount\":" + amount + ",\"method\":\"" + method + "\"}");
    }
    public void notifyUserEvent(String type, Long userId, String msg, Long branchId) {
        createNotification(type, "INFO", msg, msg, userId, "USER", branchId, 1L, null);
    }

    private String formatAmount(java.math.BigDecimal a) {
        if (a == null) return "₹0";
        return "₹" + a.toPlainString();
    }

    @Override
    public Response checkAndCreateAlerts() {
        int createdBefore = (int) alertRepository.count();
        // Low stock / out of stock per branch
        List<Product> allProducts = productRepository.findAll();
        for (Product p : allProducts) checkProductStock(p);
        // Expiry per batch
        List<Batch> allBatches = batchRepository.findAll();
        for (Batch b : allBatches) checkBatchExpiry(b);
        int createdAfter = (int) alertRepository.count();
        int created = createdAfter - createdBefore;
        return Response.builder().status(200).message(created + " new alerts created").build();
    }

    @Scheduled(fixedRate = 1800000) // 30 min — idempotent due to dedup
    @Transactional
    public void scheduledExpiryAndStockCheck() {
        log.info("Scheduled alert check running...");
        checkAndCreateAlerts();
    }

    // ================= Read / Count — branch & user aware =================
    private Long currentBranchId() {
        try {
            var t = TenantContext.get();
            if (t != null && !t.isSuperAdmin() && t.branchId() != null) return t.branchId();
        } catch (Exception ignored) {}
        return null; // super admin or no tenant -> all branches
    }

    @Override
    public Response getUnreadAlerts() {
        Long branchId = currentBranchId();
        List<Alert> alerts;
        if (branchId != null) alerts = alertRepository.findByBranchIdAndReadFalseOrderByCreatedAtDesc(branchId);
        else alerts = alertRepository.findByReadFalseOrderByCreatedAtDesc();
        // Limit to 10 for dropdown performance
        if (alerts.size() > 10) alerts = alerts.subList(0, 10);
        List<AlertDTO> dtos = alerts.stream().map(this::toDTO).toList();
        return Response.builder().status(200).alerts(dtos).build();
    }

    @Override
    public Response getAllAlerts() {
        Long branchId = currentBranchId();
        List<Alert> alerts;
        if (branchId != null) alerts = alertRepository.findByBranchIdOrderByCreatedAtDesc(branchId);
        else alerts = alertRepository.findAllByOrderByCreatedAtDesc();
        List<AlertDTO> dtos = alerts.stream().map(this::toDTO).toList();
        return Response.builder().status(200).alerts(dtos).build();
    }

    public Response getAlertsPaged(int page, int size, String type, Boolean unreadOnly) {
        Long branchId = currentBranchId();
        Page<Alert> p = alertRepository.findFiltered(branchId, unreadOnly != null && unreadOnly, type, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<AlertDTO> dtos = p.getContent().stream().map(this::toDTO).toList();
        return Response.builder().status(200).alerts(dtos)
                .totalPages(p.getTotalPages()).totalElements(p.getTotalElements()).currentPage(p.getNumber()).pageSize(p.getSize()).build();
    }

    @Override
    public Response markAsRead(Long id) {
        Alert alert = alertRepository.findById(id).orElse(null);
        if (alert != null) {
            // Security: verify branch ownership
            Long branchId = currentBranchId();
            if (branchId != null && alert.getBranchId() != null && !branchId.equals(alert.getBranchId())) {
                return Response.builder().status(403).message("Forbidden").build();
            }
            alert.setRead(true);
            alert.setReadAt(LocalDateTime.now());
            alertRepository.save(alert);
        }
        return Response.builder().status(200).message("Alert marked as read").build();
    }

    @Override
    public Response markAllAsRead() {
        Long branchId = currentBranchId();
        List<Alert> unread;
        if (branchId != null) unread = alertRepository.findByBranchIdAndReadFalseOrderByCreatedAtDesc(branchId);
        else unread = alertRepository.findByReadFalseOrderByCreatedAtDesc();
        unread.forEach(a -> { a.setRead(true); a.setReadAt(LocalDateTime.now()); });
        alertRepository.saveAll(unread);
        return Response.builder().status(200).message("All alerts marked as read").build();
    }

    @Override
    public Response getUnreadCount() {
        Long branchId = currentBranchId();
        long count;
        if (branchId != null) count = alertRepository.countByBranchIdAndReadFalse(branchId);
        else count = alertRepository.countByReadFalse();
        return Response.builder().status(200).message(String.valueOf(count)).build();
    }

    private AlertDTO toDTO(Alert a) {
        AlertDTO dto = modelMapper.map(a, AlertDTO.class);
        // Enrich branchName if available
        return dto;
    }
}
