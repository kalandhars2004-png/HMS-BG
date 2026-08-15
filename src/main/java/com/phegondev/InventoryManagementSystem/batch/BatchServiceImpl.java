package com.phegondev.InventoryManagementSystem.batch;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
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

    @Override
    public Response createBatch(BatchDTO batchDTO) {
        Batch batchToSave = modelMapper.map(batchDTO, Batch.class);
        batchRepository.save(batchToSave);

        return Response.builder()
                .status(200)
                .message("Batch created successfully")
                .build();
    }

    @Override
    public Response getAllBatches() {
        List<Batch> batches = batchRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<BatchDTO> batchDTOS = modelMapper.map(batches, new TypeToken<List<BatchDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .batches(batchDTOS)
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
