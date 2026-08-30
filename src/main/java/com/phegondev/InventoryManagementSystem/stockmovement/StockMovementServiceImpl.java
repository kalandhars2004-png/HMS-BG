package com.phegondev.InventoryManagementSystem.stockmovement;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockMovementServiceImpl implements StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    @Override
    public void record(Long productId, String productName, String productSku, String batchNo,
                       MovementType movementType, Integer quantityIn, Integer quantityOut,
                       Integer balanceStock, Long referenceId, String referenceType, String changedBy) {
        var tenant = TenantContext.get();
        Long branchId = null;
        Long orgId = 1L;
        if (tenant != null) {
            branchId = tenant.branchId();
            if (tenant.organizationId() != null) orgId = tenant.organizationId();
        }
        // Fallback to product branch if tenant branch not set (superadmin all-branches case)
        if (branchId == null) {
            // leave null to indicate global/system, but prefer 1 for legacy
        }
        StockMovement movement = StockMovement.builder()
                .branchId(branchId)
                .organizationId(orgId)
                .productId(productId)
                .productName(productName)
                .productSku(productSku)
                .batchNo(batchNo)
                .movementType(movementType)
                .quantityIn(quantityIn)
                .quantityOut(quantityOut)
                .balanceStock(balanceStock)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .changedBy(changedBy)
                .createdAt(LocalDateTime.now())
                .build();
        stockMovementRepository.save(movement);
        log.debug("StockMovement recorded: {} {} in={} out={} balance={} branch={} ref={}#{}", productName, movementType, quantityIn, quantityOut, balanceStock, branchId, referenceType, referenceId);
    }

    @Override
    public Response getAll(int page, int size, String searchText) {
        var tenant = TenantContext.get();
        Long branchId = tenant != null ? tenant.branchId() : null;
        boolean isSuper = tenant != null && tenant.isSuperAdmin();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockMovement> movementPage;

        // Branch-aware filtering — superadmin with no branch sees all (for All Branches), else scoped
        if (isSuper && branchId == null) {
            movementPage = stockMovementRepository.searchMovements(searchText, pageable);
        } else if (branchId != null) {
            movementPage = stockMovementRepository.searchMovementsByBranch(branchId, searchText, pageable);
        } else {
            // Fallback: no tenant (system call) — return all
            movementPage = stockMovementRepository.searchMovements(searchText, pageable);
        }

        return Response.builder()
                .status(200)
                .message("Stock movements retrieved successfully")
                .stockMovements(movementPage.getContent())
                .totalPages(movementPage.getTotalPages())
                .totalElements(movementPage.getTotalElements())
                .currentPage(movementPage.getNumber())
                .build();
    }
}
