package com.phegondev.InventoryManagementSystem.stockmovement;

import com.phegondev.InventoryManagementSystem.common.Response;
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
        StockMovement movement = StockMovement.builder()
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
    }

    @Override
    public Response getAll(int page, int size, String searchText) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<StockMovement> movementPage = stockMovementRepository.searchMovements(searchText, pageable);

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
