package com.phegondev.InventoryManagementSystem.stockadjustment;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response createStockAdjustment(StockAdjustmentDTO stockAdjustmentDTO) {
        StockAdjustment stockAdjustmentToSave = modelMapper.map(stockAdjustmentDTO, StockAdjustment.class);
        stockAdjustmentRepository.save(stockAdjustmentToSave);

        return Response.builder()
                .status(200)
                .message("Stock Adjustment created successfully")
                .build();
    }

    @Override
    public Response getAllStockAdjustments() {
        List<StockAdjustment> stockAdjustments = stockAdjustmentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<StockAdjustmentDTO> stockAdjustmentDTOS = modelMapper.map(stockAdjustments, new TypeToken<List<StockAdjustmentDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .stockAdjustments(stockAdjustmentDTOS)
                .build();
    }

    @Override
    public Response getStockAdjustmentById(Long id) {
        StockAdjustment stockAdjustment = stockAdjustmentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Stock Adjustment Not Found"));
        StockAdjustmentDTO stockAdjustmentDTO = modelMapper.map(stockAdjustment, StockAdjustmentDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .stockAdjustment(stockAdjustmentDTO)
                .build();
    }

    @Override
    public Response updateStockAdjustment(Long id, StockAdjustmentDTO stockAdjustmentDTO) {
        StockAdjustment existingStockAdjustment = stockAdjustmentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Stock Adjustment Not Found"));

        if (stockAdjustmentDTO.getProductId() != null) existingStockAdjustment.setProductId(stockAdjustmentDTO.getProductId());
        if (stockAdjustmentDTO.getAdjustmentType() != null) existingStockAdjustment.setAdjustmentType(stockAdjustmentDTO.getAdjustmentType());
        if (stockAdjustmentDTO.getQuantity() != null) existingStockAdjustment.setQuantity(stockAdjustmentDTO.getQuantity());
        if (stockAdjustmentDTO.getReason() != null) existingStockAdjustment.setReason(stockAdjustmentDTO.getReason());
        if (stockAdjustmentDTO.getReferenceNo() != null) existingStockAdjustment.setReferenceNo(stockAdjustmentDTO.getReferenceNo());
        stockAdjustmentRepository.save(existingStockAdjustment);

        return Response.builder()
                .status(200)
                .message("Stock Adjustment Successfully Updated")
                .build();
    }

    @Override
    public Response deleteStockAdjustment(Long id) {
        stockAdjustmentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Stock Adjustment Not Found"));

        stockAdjustmentRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Stock Adjustment Successfully Deleted")
                .build();
    }
}
