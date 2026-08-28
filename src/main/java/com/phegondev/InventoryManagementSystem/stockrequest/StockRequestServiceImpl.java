package com.phegondev.InventoryManagementSystem.stockrequest;

import com.phegondev.InventoryManagementSystem.audit.AuditWriter;
import com.phegondev.InventoryManagementSystem.branch.BranchRepository;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.inventory.Inventory;
import com.phegondev.InventoryManagementSystem.inventory.InventoryRepository;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.stocktransfer.StockTransfer;
import com.phegondev.InventoryManagementSystem.stocktransfer.StockTransferRepository;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import com.phegondev.InventoryManagementSystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockRequestServiceImpl implements StockRequestService {

    private final StockRequestRepository requestRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final StockTransferRepository transferRepository;
    private final AuditWriter auditWriter;

    private void enforceBranch(Long branchId) {
        var t = TenantContext.get();
        if (t == null) throw new AccessDeniedException("No tenant");
        if (t.isSuperAdmin()) return;
        if (!branchId.equals(t.branchId())) throw new AccessDeniedException("Branch mismatch");
    }

    @Override
    @Transactional
    public StockRequestDTO create(StockRequestDTO dto) {
        var t = TenantContext.get();
        if (t == null) throw new AccessDeniedException("No tenant");
        Long branchId = dto.getBranchId() != null ? dto.getBranchId() : t.branchId();
        if (branchId == null) branchId = 1L;
        enforceBranch(branchId);
        Long orgId = t.organizationId() != null ? t.organizationId() : 1L;
        productRepository.findById(dto.getProductId()).orElseThrow(() -> new NotFoundException("Product not found"));

        StockRequest req = StockRequest.builder()
                .organizationId(orgId)
                .branchId(branchId)
                .sourceBranchId(dto.getSourceBranchId())
                .sourceWarehouseId(dto.getSourceWarehouseId())
                .productId(dto.getProductId())
                .quantity(dto.getQuantity())
                .reason(dto.getReason())
                .status(StockRequestStatus.PENDING)
                .requestedBy(t.userId())
                .build();
        req = requestRepository.save(req);
        auditWriter.write("StockRequest", String.valueOf(req.getId()), "CREATE", "Branch " + branchId + " requested " + dto.getQuantity() + " of product " + dto.getProductId());
        return toDTO(req);
    }

    @Override
    @Transactional
    public StockRequestDTO approve(Long id) {
        StockRequest req = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        enforceBranch(req.getBranchId());
        if (req.getStatus() != StockRequestStatus.PENDING) throw new IllegalStateException("Only PENDING can be approved");
        var t = TenantContext.get();
        req.setStatus(StockRequestStatus.APPROVED);
        req.setApprovedBy(t != null ? t.userId() : null);
        req.setApprovedAt(LocalDateTime.now());
        // Auto-create transfer on approve
        StockTransfer tr = StockTransfer.builder()
                .productId(req.getProductId())
                .branchId(req.getBranchId())
                .organizationId(req.getOrganizationId())
                .fromBranchId(req.getSourceBranchId())
                .toBranchId(req.getBranchId())
                .quantity(req.getQuantity())
                .status("APPROVED")
                .description("From request #" + req.getId())
                .build();
        transferRepository.save(tr);
        requestRepository.save(req);
        auditWriter.write("StockRequest", String.valueOf(id), "APPROVE", "Approved request " + id);
        return toDTO(req);
    }

    @Override
    @Transactional
    public StockRequestDTO reject(Long id) {
        StockRequest req = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        enforceBranch(req.getBranchId());
        if (req.getStatus() != StockRequestStatus.PENDING) throw new IllegalStateException("Only PENDING can be rejected");
        req.setStatus(StockRequestStatus.REJECTED);
        requestRepository.save(req);
        auditWriter.write("StockRequest", String.valueOf(id), "REJECT", "Rejected request " + id);
        return toDTO(req);
    }

    @Override
    @Transactional
    public StockRequestDTO ship(Long id) {
        StockRequest req = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        enforceBranch(req.getBranchId());
        if (req.getStatus() != StockRequestStatus.APPROVED) throw new IllegalStateException("Only APPROVED can be shipped");
        var t = TenantContext.get();
        req.setStatus(StockRequestStatus.SHIPPED);
        req.setShippedAt(LocalDateTime.now());
        req.setShippedBy(t != null ? t.userId() : null);
        // Deduct from source branch inventory transactionally
        if (req.getSourceBranchId() != null) {
            deductSourceInventory(req);
        }
        requestRepository.save(req);
        return toDTO(req);
    }

    @Override
    @Transactional
    public StockRequestDTO receive(Long id) {
        StockRequest req = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        enforceBranch(req.getBranchId());
        if (req.getStatus() != StockRequestStatus.SHIPPED) throw new IllegalStateException("Only SHIPPED can be received");
        var t = TenantContext.get();
        req.setStatus(StockRequestStatus.RECEIVED);
        req.setReceivedAt(LocalDateTime.now());
        req.setReceivedBy(t != null ? t.userId() : null);
        receiveToInventory(req);
        requestRepository.save(req);
        return toDTO(req);
    }

    @Override
    @Transactional
    public StockRequestDTO cancel(Long id) {
        StockRequest req = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        enforceBranch(req.getBranchId());
        if (req.getStatus() != StockRequestStatus.PENDING) throw new IllegalStateException("Only PENDING can be cancelled");
        req.setStatus(StockRequestStatus.CANCELLED);
        requestRepository.save(req);
        return toDTO(req);
    }

    @Override
    public List<StockRequestDTO> getForCurrentBranch() {
        var t = TenantContext.get();
        if (t == null) throw new AccessDeniedException("No tenant");
        if (t.isSuperAdmin() && t.branchId() == null) {
            return requestRepository.findAll().stream().map(this::toDTO).toList();
        }
        Long bid = t.branchId() != null ? t.branchId() : 1L;
        return requestRepository.findByBranchIdOrderByRequestedAtDesc(bid).stream().map(this::toDTO).toList();
    }

    @Override
    public List<StockRequestDTO> getAll() {
        var t = TenantContext.get();
        if (t != null && !t.isSuperAdmin()) throw new AccessDeniedException("Only super admin");
        return requestRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public StockRequestDTO getById(Long id) {
        StockRequest req = requestRepository.findById(id).orElseThrow(() -> new NotFoundException("Request not found"));
        var t = TenantContext.get();
        if (t != null && !t.isSuperAdmin() && !req.getBranchId().equals(t.branchId())) throw new AccessDeniedException("Branch mismatch");
        return toDTO(req);
    }

    private void deductSourceInventory(StockRequest req) {
        // Simple FEFO deduction from source branch inventories
        var invList = inventoryRepository.findByBranchIdAndProductId(req.getSourceBranchId(), req.getProductId());
        int remaining = req.getQuantity();
        for (Inventory inv : invList) {
            if (remaining <= 0) break;
            int avail = inv.getAvailable();
            if (avail <= 0) continue;
            int deduct = Math.min(avail, remaining);
            inv.setQuantityOnHand(inv.getQuantityOnHand() - deduct);
            inventoryRepository.save(inv);
            remaining -= deduct;
        }
        if (remaining > 0) log.warn("Source branch {} insufficient inventory for product {} needed {} short {}", req.getSourceBranchId(), req.getProductId(), req.getQuantity(), remaining);
    }

    private void receiveToInventory(StockRequest req) {
        var existing = inventoryRepository.findByBranchIdAndProductIdAndBatchId(req.getBranchId(), req.getProductId(), null).stream().findFirst();
        if (existing.isPresent()) {
            Inventory inv = existing.get();
            inv.setQuantityOnHand(inv.getQuantityOnHand() + req.getQuantity());
            inventoryRepository.save(inv);
        } else {
            Inventory inv = Inventory.builder()
                    .organizationId(req.getOrganizationId())
                    .branchId(req.getBranchId())
                    .warehouseId(req.getSourceWarehouseId())
                    .productId(req.getProductId())
                    .quantityOnHand(req.getQuantity())
                    .build();
            inventoryRepository.save(inv);
        }
    }

    private StockRequestDTO toDTO(StockRequest r) {
        String productName = productRepository.findById(r.getProductId()).map(p -> p.getName()).orElse(String.valueOf(r.getProductId()));
        String requestedByName = r.getRequestedBy() != null ? userRepository.findById(r.getRequestedBy()).map(u -> u.getName()).orElse(null) : null;
        return StockRequestDTO.builder()
                .id(r.getId())
                .organizationId(r.getOrganizationId())
                .branchId(r.getBranchId())
                .sourceBranchId(r.getSourceBranchId())
                .sourceWarehouseId(r.getSourceWarehouseId())
                .productId(r.getProductId())
                .productName(productName)
                .quantity(r.getQuantity())
                .reason(r.getReason())
                .status(r.getStatus())
                .requestedBy(r.getRequestedBy())
                .requestedByName(requestedByName)
                .approvedBy(r.getApprovedBy())
                .requestedAt(r.getRequestedAt())
                .approvedAt(r.getApprovedAt())
                .shippedAt(r.getShippedAt())
                .receivedAt(r.getReceivedAt())
                .build();
    }
}
