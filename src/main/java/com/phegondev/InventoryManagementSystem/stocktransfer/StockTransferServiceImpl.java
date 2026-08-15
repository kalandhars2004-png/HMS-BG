package com.phegondev.InventoryManagementSystem.stocktransfer;

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
public class StockTransferServiceImpl implements StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response createStockTransfer(StockTransferDTO stockTransferDTO) {
        StockTransfer stockTransferToSave = modelMapper.map(stockTransferDTO, StockTransfer.class);
        stockTransferRepository.save(stockTransferToSave);

        return Response.builder()
                .status(200)
                .message("StockTransfer created successfully")
                .build();
    }

    @Override
    public Response getAllStockTransfers() {

        List<StockTransfer> stockTransfers = stockTransferRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<StockTransferDTO> stockTransferDTOS = modelMapper.map(stockTransfers, new TypeToken<List<StockTransferDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .stockTransfers(stockTransferDTOS)
                .build();
    }

    @Override
    public Response getStockTransferById(Long id) {

        StockTransfer stockTransfer = stockTransferRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("StockTransfer Not Found"));
        StockTransferDTO stockTransferDTO = modelMapper.map(stockTransfer, StockTransferDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .stockTransfer(stockTransferDTO)
                .build();
    }

    @Override
    public Response updateStockTransfer(Long id, StockTransferDTO stockTransferDTO) {

        StockTransfer existingStockTransfer = stockTransferRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("StockTransfer Not Found"));

        if (stockTransferDTO.getProductId() != null) existingStockTransfer.setProductId(stockTransferDTO.getProductId());
        if (stockTransferDTO.getFromWarehouseId() != null) existingStockTransfer.setFromWarehouseId(stockTransferDTO.getFromWarehouseId());
        if (stockTransferDTO.getToWarehouseId() != null) existingStockTransfer.setToWarehouseId(stockTransferDTO.getToWarehouseId());
        if (stockTransferDTO.getQuantity() != null) existingStockTransfer.setQuantity(stockTransferDTO.getQuantity());
        if (stockTransferDTO.getDescription() != null) existingStockTransfer.setDescription(stockTransferDTO.getDescription());
        if (stockTransferDTO.getStatus() != null) existingStockTransfer.setStatus(stockTransferDTO.getStatus());
        stockTransferRepository.save(existingStockTransfer);

        return Response.builder()
                .status(200)
                .message("StockTransfer Successfully Updated")
                .build();

    }

    @Override
    public Response deleteStockTransfer(Long id) {

         stockTransferRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("StockTransfer Not Found"));

        stockTransferRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("StockTransfer Successfully Deleted")
                .build();
    }
}
