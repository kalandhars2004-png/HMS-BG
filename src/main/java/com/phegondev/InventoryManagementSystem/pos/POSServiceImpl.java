package com.phegondev.InventoryManagementSystem.pos;

import com.phegondev.InventoryManagementSystem.alert.Alert;
import com.phegondev.InventoryManagementSystem.alert.AlertRepository;
import com.phegondev.InventoryManagementSystem.audit.AuditService;
import com.phegondev.InventoryManagementSystem.batch.Batch;
import com.phegondev.InventoryManagementSystem.batch.BatchRepository;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.customer.Customer;
import com.phegondev.InventoryManagementSystem.customer.CustomerRepository;
import com.phegondev.InventoryManagementSystem.exceptions.InsufficientStockException;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.inventory.InventoryMovement;
import com.phegondev.InventoryManagementSystem.inventory.InventoryMovementRepository;
import com.phegondev.InventoryManagementSystem.invoice.*;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.user.User;
import com.phegondev.InventoryManagementSystem.user.UserRepository;
import com.phegondev.InventoryManagementSystem.warehouse.Warehouse;
import com.phegondev.InventoryManagementSystem.warehouse.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class POSServiceImpl implements POSService {

    private final POSSessionRepository sessionRepository;
    private final POSTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;
    private final WarehouseRepository warehouseRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final AlertRepository alertRepository;
    private final AuditService auditService;
    private final ModelMapper modelMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Response openSession(POSSessionDTO sessionDTO) {
        List<POSSession> openSessions = sessionRepository.findByStatus("OPEN");
        if (!openSessions.isEmpty()) {
            return Response.builder()
                    .status(400)
                    .message("An active session is already open")
                    .build();
        }

        POSSession session = modelMapper.map(sessionDTO, POSSession.class);
        session.setSessionNumber(generateSessionNumber());

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));
        session.setOpenedBy(currentUser.getId());
        // Branch scoping §17
        var tenant = com.phegondev.InventoryManagementSystem.tenant.TenantContext.get();
        if (tenant != null && tenant.branchId() != null) {
            session.setBranchId(tenant.branchId());
            session.setOrganizationId(tenant.organizationId());
        } else if (currentUser.getBranchId() != null) {
            session.setBranchId(currentUser.getBranchId());
            session.setOrganizationId(currentUser.getOrganizationId() != null ? currentUser.getOrganizationId() : 1L);
        } else {
            session.setBranchId(1L);
            session.setOrganizationId(1L);
        }

        session.setStatus("OPEN");
        session.setOpenedAt(LocalDateTime.now());
        session.setTotalSales(BigDecimal.ZERO);
        session.setTotalRefunds(BigDecimal.ZERO);
        session.setNetAmount(BigDecimal.ZERO);
        POSSession saved = sessionRepository.save(session);

        POSSessionDTO dto = mapSessionWithUsers(saved);

        auditService.log("POS_SESSION", saved.getId(), "OPEN", "POS session opened", email, null, null);

        return Response.builder()
                .status(200)
                .message("POS session opened successfully")
                .posSession(dto)
                .build();
    }

    @Override
    @Transactional
    public Response closeSession(Long sessionId, BigDecimal closingBalance) {
        POSSession session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new NotFoundException("POS Session not found"));

        if ("CLOSED".equals(session.getStatus())) {
            return Response.builder()
                    .status(400)
                    .message("Session is already closed")
                    .build();
        }

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        session.setClosingBalance(closingBalance);
        session.setClosedAt(LocalDateTime.now());
        session.setClosedBy(currentUser.getId());
        session.setStatus("CLOSED");
        POSSession saved = sessionRepository.save(session);

        POSSessionDTO dto = mapSessionWithUsers(saved);

        auditService.log("POS_SESSION", saved.getId(), "CLOSE", "POS session closed", email, null, null);

        return Response.builder()
                .status(200)
                .message("POS session closed successfully")
                .posSession(dto)
                .build();
    }

    @Override
    public Response getActiveSession() {
        List<POSSession> openSessions = sessionRepository.findByStatus("OPEN");
        if (openSessions.isEmpty()) {
            return Response.builder()
                    .status(200)
                    .message("No active session")
                    .build();
        }
        POSSession session = openSessions.get(0);
        POSSessionDTO dto = mapSessionWithUsers(session);

        return Response.builder()
                .status(200)
                .message("success")
                .posSession(dto)
                .build();
    }

    @Override
    public Response getAllSessions() {
        List<POSSession> sessions = sessionRepository.findAllByOrderByOpenedAtDesc();
        List<POSSessionDTO> dtos = sessions.stream()
                .map(this::mapSessionWithUsers)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("success")
                .posSessions(dtos)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response addTransaction(Long sessionId, POSTransactionDTO transactionDTO) {
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Validate session (with lock)
        POSSession session = sessionRepository.findByIdWithLock(sessionId)
                .orElseThrow(() -> new NotFoundException("POS Session not found"));
        if (!"OPEN".equals(session.getStatus())) {
            return Response.builder().status(400).message("Session is not open").build();
        }

        // 2. Validate product (with lock)
        Product product = productRepository.findByIdWithLock(transactionDTO.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (Boolean.TRUE.equals(product.getPrescriptionRequired())) {
            return Response.builder().status(400).message("Prescription required for: " + product.getName()).build();
        }

        // 3. FEFO batch selection & stock reduction
        int qtyToSell = transactionDTO.getQuantity();
        if (qtyToSell <= 0) {
            return Response.builder().status(400).message("Quantity must be positive").build();
        }
        int previousStock = product.getStockQuantity();

        if (product.getStockQuantity() < qtyToSell) {
            throw new InsufficientStockException("Insufficient stock for: " + product.getName() +
                    ". Available: " + product.getStockQuantity() + ", Requested: " + qtyToSell);
        }

        List<Batch> availableBatches = batchRepository
                .findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(product.getId(), 0);

        if (!availableBatches.isEmpty()) {
            int remaining = qtyToSell;
            List<Batch> updatedBatches = new ArrayList<>();
            boolean hasUsableBatch = false;

            for (Batch batch : availableBatches) {
                if (remaining <= 0) break;
                if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDateTime.now())) {
                    continue;
                }
                hasUsableBatch = true;
                int take = Math.min(remaining, batch.getQuantity());
                batch.setQuantity(batch.getQuantity() - take);
                updatedBatches.add(batch);
                remaining -= take;
            }

            if (remaining > 0) {
                String msg = hasUsableBatch
                        ? "Insufficient batch stock for: " + product.getName()
                        : "All batches for " + product.getName() + " are expired";
                throw new InsufficientStockException(msg);
            }

            batchRepository.saveAll(updatedBatches);
        }

        // 4. Reduce product stock
        product.setStockQuantity(product.getStockQuantity() - qtyToSell);
        productRepository.save(product);

        // 5. Reduce warehouse stock (with lock)
        if (product.getWarehouseId() != null) {
            warehouseRepository.findByIdWithLock(product.getWarehouseId()).ifPresent(w -> {
                if (w.getStock() != null) w.setStock(w.getStock() - qtyToSell);
                if (w.getQty() != null) w.setQty(w.getQty() - qtyToSell);
                warehouseRepository.save(w);
            });
        }

        // 6. Create POS transaction record
        POSTransaction transaction = modelMapper.map(transactionDTO, POSTransaction.class);
        transaction.setSessionId(sessionId);
        transaction.setReceiptNumber(generateReceiptNumber());
        transaction.setStatus("COMPLETED");
        transaction.setUnitPrice(product.getPrice());
        // Biller: whoever the POS popup selected, otherwise the logged-in user
        if (transactionDTO.getBillerId() != null) {
            userRepository.findById(transactionDTO.getBillerId())
                    .ifPresent(b -> {
                        transaction.setBillerId(b.getId());
                        transaction.setBillerName(b.getName());
                    });
        }
        if (transaction.getBillerId() == null) {
            userRepository.findByEmail(performedBy).ifPresent(b -> {
                transaction.setBillerId(b.getId());
                transaction.setBillerName(b.getName());
            });
        }
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(qtyToSell));
        transaction.setTotalPrice(totalPrice);
        // Branch ownership §6
        if (session.getBranchId() != null) transaction.setBranchId(session.getBranchId());
        else {
            var t = com.phegondev.InventoryManagementSystem.tenant.TenantContext.get();
            transaction.setBranchId(t != null && t.branchId() != null ? t.branchId() : 1L);
        }
        transaction.setOrganizationId(session.getOrganizationId() != null ? session.getOrganizationId() : 1L);
        POSTransaction saved = transactionRepository.save(transaction);

        // 7. Update session totals (under lock from step 1)
        session.setTotalSales(session.getTotalSales().add(totalPrice));
        session.setNetAmount(session.getTotalSales().subtract(session.getTotalRefunds()));
        sessionRepository.save(session);

        // 8. Create invoice (thread-safe number generation)
        String invoiceNumber = generateInvoiceNumber();
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .transactionId(saved.getId())
                .customerName(transactionDTO.getCustomerName())
                .subtotal(product.getPrice().multiply(BigDecimal.valueOf(qtyToSell)))
                .taxAmount(transactionDTO.getTaxAmount() != null ?
                        transactionDTO.getTaxAmount() : BigDecimal.ZERO)
                .discountAmount(transactionDTO.getDiscountAmount() != null ?
                        transactionDTO.getDiscountAmount() : BigDecimal.ZERO)
                .totalAmount(totalPrice)
                .status("PAID")
                .paymentMethod(transactionDTO.getPaymentMethod())
                .invoiceDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        Invoice savedInvoice = invoiceRepository.save(invoice);

        InvoiceItem invoiceItem = InvoiceItem.builder()
                .invoiceId(savedInvoice.getId())
                .productId(product.getId())
                .quantity(qtyToSell)
                .unitPrice(product.getPrice())
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(qtyToSell)))
                .build();
        invoiceItemRepository.save(invoiceItem);

        // 9. Inventory movement log
        InventoryMovement movement = InventoryMovement.builder()
                .movementType("SALE")
                .referenceType("POS")
                .referenceNumber(saved.getReceiptNumber())
                .productId(product.getId())
                .productName(product.getName())
                .warehouseId(product.getWarehouseId())
                .quantity(qtyToSell)
                .previousStock(previousStock)
                .currentStock(product.getStockQuantity())
                .invoiceNumber(invoiceNumber)
                .performedBy(performedBy)
                .build();
        inventoryMovementRepository.save(movement);

        // 10. Update / create customer (by phone, fallback to name)
        if (transactionDTO.getCustomerName() != null && !transactionDTO.getCustomerName().isBlank()
                && !"Walk-in Customer".equals(transactionDTO.getCustomerName())) {
            Optional<Customer> existingCustomer = Optional.empty();
            if (transactionDTO.getCustomerPhone() != null && !transactionDTO.getCustomerPhone().isBlank()) {
                existingCustomer = customerRepository.findByPhone(transactionDTO.getCustomerPhone());
            }
            if (existingCustomer.isEmpty()) {
                existingCustomer = customerRepository.findByName(transactionDTO.getCustomerName());
            }
            Customer customer;
            if (existingCustomer.isPresent()) {
                customer = existingCustomer.get();
                customer.setLifetimeSpend(customer.getLifetimeSpend().add(totalPrice));
                customer.setPurchaseCount(customer.getPurchaseCount() + 1);
                customer.setLastPurchase(LocalDateTime.now());
            } else {
                customer = Customer.builder()
                        .name(transactionDTO.getCustomerName())
                        .phone(transactionDTO.getCustomerPhone())
                        .lifetimeSpend(totalPrice)
                        .purchaseCount(1)
                        .lastPurchase(LocalDateTime.now())
                        .loyaltyPoints(qtyToSell)
                        .build();
            }
            customerRepository.save(customer);
        }

        // 11. Low stock alert (dedup)
        if (product.getLowStockQuantity() != null && product.getStockQuantity() <= product.getLowStockQuantity()) {
            Optional<Alert> existingAlert = alertRepository
                    .findFirstByTypeAndRelatedEntityIdAndReadFalseOrderByCreatedAtDesc(
                            "LOW_STOCK", product.getId());
            if (existingAlert.isEmpty()) {
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
            }
        }

        // 12. Out of stock
        if (product.getStockQuantity() <= 0) {
            Optional<Alert> existingOutOfStock = alertRepository
                    .findFirstByTypeAndRelatedEntityIdAndReadFalseOrderByCreatedAtDesc(
                            "OUT_OF_STOCK", product.getId());
            if (existingOutOfStock.isEmpty()) {
                Alert alert = Alert.builder()
                        .type("OUT_OF_STOCK")
                        .severity("CRITICAL")
                        .title("Out of Stock: " + product.getName())
                        .message("Product " + product.getName() + " is now out of stock")
                        .relatedEntityId(product.getId())
                        .relatedEntityType("PRODUCT")
                        .read(false)
                        .createdAt(LocalDateTime.now())
                        .build();
                alertRepository.save(alert);
            }
        }

        // 13. Audit log
        auditService.log("SALE", saved.getId(), "COMPLETE",
                "Sale completed: " + product.getName() + " x" + qtyToSell +
                        " = " + totalPrice + " | Invoice: " + invoiceNumber,
                performedBy,
                "stock:" + previousStock,
                "stock:" + product.getStockQuantity());

        POSTransactionDTO dto = mapTransactionWithProduct(saved);

        return Response.builder()
                .status(200)
                .message("Transaction completed successfully")
                .posTransaction(dto)
                .build();
    }

    @Override
    public Response getSessionTransactions(Long sessionId) {
        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("POS Session not found"));

        List<POSTransaction> transactions = transactionRepository.findBySessionId(sessionId);
        List<POSTransactionDTO> dtos = transactions.stream()
                .map(this::mapTransactionWithProduct)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("success")
                .posTransactions(dtos)
                .build();
    }

    @Override
    @Transactional
    public Response voidTransaction(Long transactionId) {
        String performedBy = SecurityContextHolder.getContext().getAuthentication().getName();

        POSTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));

        if (!"COMPLETED".equals(transaction.getStatus())) {
            return Response.builder()
                    .status(400)
                    .message("Transaction cannot be voided")
                    .build();
        }

        Product product = productRepository.findByIdWithLock(transaction.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        int qtyToRestore = transaction.getQuantity();
        int previousStock = product.getStockQuantity();
        product.setStockQuantity(product.getStockQuantity() + qtyToRestore);
        productRepository.save(product);

        // Restore warehouse stock
        if (product.getWarehouseId() != null) {
            warehouseRepository.findByIdWithLock(product.getWarehouseId()).ifPresent(w -> {
                if (w.getStock() != null) w.setStock(w.getStock() + qtyToRestore);
                if (w.getQty() != null) w.setQty(w.getQty() + qtyToRestore);
                warehouseRepository.save(w);
            });
        }

        transaction.setStatus("VOID");
        transactionRepository.save(transaction);

        // Restore batch stock to nearest-expiring batch (reverse of FEFO deduction)
        List<Batch> batches = batchRepository
                .findByProductIdAndQuantityGreaterThanOrderByExpiryDateAsc(product.getId(), -1);
        if (!batches.isEmpty()) {
            int remaining = qtyToRestore;
            for (Batch batch : batches) {
                if (remaining <= 0) break;
                batch.setQuantity(batch.getQuantity() + remaining);
                remaining = 0;
                batchRepository.save(batch);
            }
        }

        POSSession session = sessionRepository.findByIdWithLock(transaction.getSessionId()).orElse(null);
        if (session != null) {
            session.setTotalRefunds(session.getTotalRefunds().add(transaction.getTotalPrice()));
            session.setNetAmount(session.getTotalSales().subtract(session.getTotalRefunds()));
            sessionRepository.save(session);
        }

        // Inventory movement for void
        InventoryMovement movement = InventoryMovement.builder()
                .movementType("VOID")
                .referenceType("POS")
                .referenceNumber(transaction.getReceiptNumber())
                .productId(product.getId())
                .productName(product.getName())
                .quantity(transaction.getQuantity())
                .previousStock(previousStock)
                .currentStock(product.getStockQuantity())
                .performedBy(performedBy)
                .build();
        inventoryMovementRepository.save(movement);

        // Audit log
        auditService.log("SALE", transaction.getId(), "VOID",
                "Sale voided: " + product.getName() + " x" + transaction.getQuantity(),
                performedBy,
                "stock:" + previousStock,
                "stock:" + product.getStockQuantity());

        return Response.builder()
                .status(200)
                .message("Transaction voided successfully")
                .build();
    }

    @Override
    public Response getDailySales() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);

        List<POSTransaction> todayTransactions = transactionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(t -> t.getCreatedAt() != null
                        && !t.getCreatedAt().isBefore(start)
                        && !t.getCreatedAt().isAfter(end))
                .collect(Collectors.toList());

        BigDecimal totalSales = todayTransactions.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .map(POSTransaction::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRefunds = todayTransactions.stream()
                .filter(t -> "REFUNDED".equals(t.getStatus()))
                .map(POSTransaction::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long transactionCount = todayTransactions.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();

        return Response.builder()
                .status(200)
                .message("success")
                .totalSales(totalSales)
                .totalRefunds(totalRefunds)
                .transactionCount(transactionCount)
                .build();
    }

    private String generateSessionNumber() {
        Long maxId = (Long) entityManager
                .createQuery("select max(s.id) from POSSession s")
                .getSingleResult();
        long next = (maxId != null ? maxId : 0) + 1;
        String year = String.valueOf(LocalDateTime.now().getYear());
        return "SES-" + year + "-" + String.format("%04d", next);
    }

    private String generateReceiptNumber() {
        Long maxId = (Long) entityManager
                .createQuery("select max(t.id) from POSTransaction t")
                .getSingleResult();
        long next = (maxId != null ? maxId : 0) + 1;
        String year = String.valueOf(LocalDateTime.now().getYear());
        return "RCP-" + year + "-" + String.format("%06d", next);
    }

    private String generateInvoiceNumber() {
        Long maxId = (Long) entityManager
                .createQuery("select max(i.id) from Invoice i")
                .getSingleResult();
        long next = (maxId != null ? maxId : 0) + 1;
        String year = String.valueOf(LocalDateTime.now().getYear());
        return "INV-" + year + "-" + String.format("%06d", next);
    }

    private POSSessionDTO mapSessionWithUsers(POSSession session) {
        POSSessionDTO dto = modelMapper.map(session, POSSessionDTO.class);
        if (session.getOpenedBy() != null) {
            userRepository.findById(session.getOpenedBy()).ifPresent(u -> dto.setOpenedByName(u.getName()));
        }
        if (session.getClosedBy() != null) {
            userRepository.findById(session.getClosedBy()).ifPresent(u -> dto.setClosedByName(u.getName()));
        }
        return dto;
    }

    private POSTransactionDTO mapTransactionWithProduct(POSTransaction transaction) {
        POSTransactionDTO dto = modelMapper.map(transaction, POSTransactionDTO.class);
        if (transaction.getProductId() != null) {
            productRepository.findById(transaction.getProductId())
                    .ifPresent(p -> dto.setProductName(p.getName()));
        }
        if (dto.getBillerName() == null && transaction.getBillerId() != null) {
            userRepository.findById(transaction.getBillerId())
                    .ifPresent(b -> dto.setBillerName(b.getName()));
        }
        return dto;
    }
}