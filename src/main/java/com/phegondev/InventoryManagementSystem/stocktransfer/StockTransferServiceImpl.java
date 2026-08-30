package com.phegondev.InventoryManagementSystem.stocktransfer;

import com.phegondev.InventoryManagementSystem.branch.Branch;
import com.phegondev.InventoryManagementSystem.branch.BranchRepository;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryManagementSystem.alert.AlertService;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import com.phegondev.InventoryManagementSystem.user.User;
import com.phegondev.InventoryManagementSystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AlertService alertService;

    @Override
    @Transactional
    public Response createStockTransfer(StockTransferDTO dto) {
        var tenant = TenantContext.get();
        if (tenant == null) throw new AccessDeniedException("Not authenticated");

        Long callerBranchId = tenant.branchId();
        boolean isSuper = tenant.isSuperAdmin();
        Long orgId = tenant.organizationId() != null ? tenant.organizationId() : 1L;

        // Validate product
        if (dto.getProductId() == null) throw new NameValueRequiredException("productId is required");
        productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new NotFoundException("Product Not Found: " + dto.getProductId()));
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) throw new NameValueRequiredException("quantity must be > 0");

        Long fromBranchId = dto.getFromBranchId();
        Long toBranchId = dto.getToBranchId();

        // Also support legacy warehouse fields as branch fallback
        if (fromBranchId == null && dto.getFromWarehouseId() != null) fromBranchId = dto.getFromWarehouseId();
        if (toBranchId == null && dto.getToWarehouseId() != null) toBranchId = dto.getToWarehouseId();

        if (!isSuper) {
            if (callerBranchId == null) throw new AccessDeniedException("Branch user must have a branch");
            // From strictly caller's branch
            if (fromBranchId != null && !fromBranchId.equals(callerBranchId)) {
                throw new AccessDeniedException("From branch must be your branch (" + callerBranchId + ")");
            }
            fromBranchId = callerBranchId;
        }

        if (fromBranchId == null) throw new NameValueRequiredException("fromBranchId is required");
        if (toBranchId == null) throw new NameValueRequiredException("toBranchId is required");
        if (fromBranchId.equals(toBranchId)) throw new NameValueRequiredException("From and To branches must differ");

        final Long finalFromBranchId = fromBranchId;
        final Long finalToBranchId = toBranchId;
        Branch fromBranch = branchRepository.findById(finalFromBranchId)
                .orElseThrow(() -> new NotFoundException("From Branch not found: " + finalFromBranchId));
        Branch toBranch = branchRepository.findById(finalToBranchId)
                .orElseThrow(() -> new NotFoundException("To Branch not found: " + finalToBranchId));

        StockTransfer toSave = StockTransfer.builder()
                .branchId(fromBranchId)
                .organizationId(orgId)
                .productId(dto.getProductId())
                .fromBranchId(fromBranchId)
                .toBranchId(toBranchId)
                .fromWarehouseId(dto.getFromWarehouseId())
                .toWarehouseId(dto.getToWarehouseId())
                .quantity(dto.getQuantity())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : "PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        StockTransfer saved = stockTransferRepository.save(toSave);
        log.info("Stock transfer {} created: {} units product {} from branch {} -> {}", saved.getId(), saved.getQuantity(), saved.getProductId(), fromBranchId, toBranchId);

        // Notify TO branch — App notification (Alerts) + Email/SMS (log placeholder for now)
        try {
            String title = "New Stock Transfer — " + saved.getQuantity() + " units";
            String msg = "Transfer #" + saved.getId() + ": " + saved.getQuantity() + " units from " + fromBranch.getName() + " to " + toBranch.getName()
                    + " — product " + saved.getProductId() + ". Please confirm receipt.";
            String metadata = "{\"transferId\":" + saved.getId() + ",\"productId\":" + saved.getProductId()
                    + ",\"quantity\":" + saved.getQuantity() + ",\"fromBranchId\":" + fromBranchId + ",\"toBranchId\":" + toBranchId + "}";
            alertService.createNotification("STOCK_TRANSFER", "INFO", title, msg, saved.getId(), "STOCK_TRANSFER", toBranchId, orgId, metadata);

            // Email/SMS to To Branch manager(s)
            List<User> toBranchUsers = userRepository.findByBranchId(toBranchId);
            // Also manager via branch.managerId
            if (toBranch.getManagerId() != null) {
                userRepository.findById(toBranch.getManagerId()).ifPresent(m -> {
                    if (toBranchUsers.stream().noneMatch(u -> u.getId().equals(m.getId()))) toBranchUsers.add(m);
                });
            }
            for (User u : toBranchUsers) {
                if (u.getEmail() != null) {
                    log.info("[Notification] Email to {} <{}> — Stock Transfer #{} from {} to {} ({} units)", u.getName(), u.getEmail(), saved.getId(), fromBranch.getName(), toBranch.getName(), saved.getQuantity());
                    // TODO integrate EmailService.send(u.getEmail(), title, msg)
                }
                if (u.getPhoneNumber() != null) {
                    log.info("[Notification] SMS to {} <{}> — Transfer #{} {} units pending", u.getName(), u.getPhoneNumber(), saved.getId(), saved.getQuantity());
                    // TODO integrate SmsService.send(u.getPhoneNumber(), msg)
                }
                // Also create user-targeted alert for each manager for app inbox filtering
                try {
                    alertService.createNotification("STOCK_TRANSFER", "INFO", title, msg + " (for " + u.getName() + ")", saved.getId(), "STOCK_TRANSFER", toBranchId, orgId, metadata);
                } catch (Exception e) { log.debug("Per-user alert failed for {}", u.getId(), e); }
            }
            if (toBranchUsers.isEmpty()) {
                log.info("[Notification] No users found for To Branch {} — only branch-scoped alert created", toBranchId);
            }
        } catch (Exception e) {
            log.warn("Stock transfer notification failed for transfer {}", saved.getId(), e);
        }

        return Response.builder()
                .status(200)
                .message("StockTransfer created successfully")
                .build();
    }

    @Override
    public Response getAllStockTransfers() {
        var tenant = TenantContext.get();
        Long branchId = tenant != null ? tenant.branchId() : null;
        boolean isSuper = tenant != null && tenant.isSuperAdmin();

        List<StockTransfer> list;
        if (isSuper || branchId == null) {
            list = stockTransferRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        } else {
            // Branch manager sees outbound + inbound
            List<StockTransfer> from = stockTransferRepository.findByFromBranchId(branchId);
            List<StockTransfer> to = stockTransferRepository.findByToBranchId(branchId);
            Map<Long, StockTransfer> merged = new LinkedHashMap<>();
            for (StockTransfer s : from) merged.put(s.getId(), s);
            for (StockTransfer s : to) merged.put(s.getId(), s);
            list = new ArrayList<>(merged.values());
            list.sort(Comparator.comparing(StockTransfer::getId).reversed());
        }

        List<StockTransferDTO> dtos = list.stream().map(this::toDTO).collect(Collectors.toList());
        return Response.builder()
                .status(200)
                .message("success")
                .stockTransfers(dtos)
                .build();
    }

    @Override
    public Response getStockTransferById(Long id) {
        StockTransfer st = stockTransferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StockTransfer Not Found"));
        // Branch isolation check
        var tenant = TenantContext.get();
        if (tenant != null && !tenant.isSuperAdmin() && tenant.branchId() != null) {
            Long b = tenant.branchId();
            if (!b.equals(st.getFromBranchId()) && !b.equals(st.getToBranchId()) && !b.equals(st.getBranchId())) {
                throw new AccessDeniedException("Not authorized for this transfer");
            }
        }
        return Response.builder()
                .status(200)
                .message("success")
                .stockTransfer(toDTO(st))
                .build();
    }

    @Override
    public Response updateStockTransfer(Long id, StockTransferDTO dto) {
        StockTransfer existing = stockTransferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StockTransfer Not Found"));

        var tenant = TenantContext.get();
        if (tenant != null && !tenant.isSuperAdmin() && tenant.branchId() != null) {
            // Only owner branch can update
            if (!tenant.branchId().equals(existing.getFromBranchId()) && !tenant.branchId().equals(existing.getBranchId())) {
                throw new AccessDeniedException("Only source branch can update this transfer");
            }
        }

        if (dto.getProductId() != null) existing.setProductId(dto.getProductId());
        if (dto.getFromWarehouseId() != null) existing.setFromWarehouseId(dto.getFromWarehouseId());
        if (dto.getToWarehouseId() != null) existing.setToWarehouseId(dto.getToWarehouseId());
        // Branch fields — super can change, branch manager cannot change from
        if (dto.getToBranchId() != null) {
            if (existing.getFromBranchId() != null && dto.getToBranchId().equals(existing.getFromBranchId())) {
                throw new NameValueRequiredException("From and To branches must differ");
            }
            branchRepository.findById(dto.getToBranchId()).orElseThrow(() -> new NotFoundException("To Branch not found"));
            existing.setToBranchId(dto.getToBranchId());
        }
        if (dto.getFromBranchId() != null) {
            var t = TenantContext.get();
            boolean isSuper = t != null && t.isSuperAdmin();
            if (!isSuper) throw new AccessDeniedException("From branch cannot be changed by branch user");
            branchRepository.findById(dto.getFromBranchId()).orElseThrow(() -> new NotFoundException("From Branch not found"));
            existing.setFromBranchId(dto.getFromBranchId());
            existing.setBranchId(dto.getFromBranchId());
        }
        if (dto.getQuantity() != null) existing.setQuantity(dto.getQuantity());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        stockTransferRepository.save(existing);

        return Response.builder()
                .status(200)
                .message("StockTransfer Successfully Updated")
                .build();
    }

    @Override
    public Response deleteStockTransfer(Long id) {
        StockTransfer existing = stockTransferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("StockTransfer Not Found"));
        var tenant = TenantContext.get();
        if (tenant != null && !tenant.isSuperAdmin() && tenant.branchId() != null) {
            if (!tenant.branchId().equals(existing.getFromBranchId()) && !tenant.branchId().equals(existing.getBranchId())) {
                throw new AccessDeniedException("Only source branch can delete this transfer");
            }
        }
        stockTransferRepository.deleteById(id);
        return Response.builder()
                .status(200)
                .message("StockTransfer Successfully Deleted")
                .build();
    }

    private StockTransferDTO toDTO(StockTransfer st) {
        StockTransferDTO dto = modelMapper.map(st, StockTransferDTO.class);
        try {
            if (st.getFromBranchId() != null) {
                branchRepository.findById(st.getFromBranchId()).ifPresent(b -> dto.setFromBranchName(b.getName()));
            }
            if (st.getToBranchId() != null) {
                branchRepository.findById(st.getToBranchId()).ifPresent(b -> dto.setToBranchName(b.getName()));
            }
            if (st.getProductId() != null) {
                productRepository.findById(st.getProductId()).ifPresent(p -> dto.setProductName(p.getName()));
            }
            // Backfill warehouse names for legacy UI
            // (warehouses are branches of type WAREHOUSE, keep name lookup if needed)
        } catch (Exception e) { log.debug("Enrich transfer {} failed", st.getId(), e); }
        return dto;
    }
}
