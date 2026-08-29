package com.phegondev.InventoryManagementSystem.batch;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.alert.AlertService;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final ModelMapper modelMapper;
    private final @Lazy AlertService alertService;

    @Override
    public Response createBatch(BatchDTO batchDTO) {
        Batch batchToSave = modelMapper.map(batchDTO, Batch.class);
        var t = TenantContext.get();
        if (t != null) {
            if (t.branchId() != null) batchToSave.setBranchId(t.branchId());
            else if (t.isSuperAdmin()) batchToSave.setBranchId(1L);
            batchToSave.setOrganizationId(t.organizationId() != null ? t.organizationId() : 1L);
        } else {
            batchToSave.setBranchId(1L);
            batchToSave.setOrganizationId(1L);
        }
        batchRepository.save(batchToSave);
        try { alertService.checkBatchExpiry(batchToSave); } catch (Exception e) { log.warn("Alert hook failed for batch {}", batchToSave.getId(), e); }

        return Response.builder()
                .status(200)
                .message("Batch created successfully")
                .build();
    }

    @Override
    public Response getAllBatches(Integer page, Integer size) {

        if (page == null || size == null) {
            List<Batch> batches = batchRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            return Response.builder()
                    .status(200)
                    .message("success")
                    .batches(modelMapper.map(batches, new TypeToken<List<BatchDTO>>() {}.getType()))
                    .build();
        }

        // Batch tables grow fastest in pharmacy inventory (one row per delivery
        // lot), so unbounded listing degrades first here.
        Page<Batch> batchPage = batchRepository.findAll(
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                        Sort.by(Sort.Direction.DESC, "id")));

        return Response.builder()
                .status(200)
                .message("success")
                .batches(modelMapper.map(batchPage.getContent(), new TypeToken<List<BatchDTO>>() {}.getType()))
                .totalPages(batchPage.getTotalPages())
                .totalElements(batchPage.getTotalElements())
                .currentPage(batchPage.getNumber())
                .pageSize(batchPage.getSize())
                .build();
    }

    @Override
    public Response getBatchById(Long id) {
        Batch batch = batchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Batch Not Found"));
        BatchDTO batchDTO = modelMapper.map(batch, BatchDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .batch(batchDTO)
                .build();
    }

    @Override
    public Response updateBatch(Long id, BatchDTO batchDTO) {
        Batch existingBatch = batchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Batch Not Found"));

        if (batchDTO.getProductId() != null) existingBatch.setProductId(batchDTO.getProductId());
        if (batchDTO.getBatchNo() != null) existingBatch.setBatchNo(batchDTO.getBatchNo());
        if (batchDTO.getQuantity() != null) existingBatch.setQuantity(batchDTO.getQuantity());
        if (batchDTO.getMrp() != null) existingBatch.setMrp(batchDTO.getMrp());
        if (batchDTO.getPurchasePrice() != null) existingBatch.setPurchasePrice(batchDTO.getPurchasePrice());
        if (batchDTO.getManufacturingDate() != null) existingBatch.setManufacturingDate(batchDTO.getManufacturingDate());
        if (batchDTO.getExpiryDate() != null) existingBatch.setExpiryDate(batchDTO.getExpiryDate());
        if (batchDTO.getStatus() != null) existingBatch.setStatus(batchDTO.getStatus());
        batchRepository.save(existingBatch);
        try { alertService.checkBatchExpiry(existingBatch); } catch (Exception e) { log.warn("Alert hook failed for batch {}", existingBatch.getId(), e); }

        return Response.builder()
                .status(200)
                .message("Batch Successfully Updated")
                .build();
    }

    @Override
    public Response deleteBatch(Long id) {
        batchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Batch Not Found"));

        batchRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Batch Successfully Deleted")
                .build();
    }

    @Override
    public Response getBatchesExpiringBefore(LocalDateTime date) {
        List<Batch> batches = batchRepository.findByExpiryDateBefore(date);
        List<BatchDTO> batchDTOS = modelMapper.map(batches, new TypeToken<List<BatchDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .batches(batchDTOS)
                .build();
    }
}
