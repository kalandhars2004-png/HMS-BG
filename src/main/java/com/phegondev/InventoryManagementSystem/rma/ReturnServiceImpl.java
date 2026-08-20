package com.phegondev.InventoryManagementSystem.rma;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnServiceImpl implements ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final ReturnItemRepository returnItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    private String generateReturnNumber() {
        // Year no longer hardcoded to 2026, and max(id)+1 so deleted rows don't
        // cause number reuse.
        Long maxId = returnRequestRepository.findMaxId();
        long next = (maxId != null ? maxId : 0) + 1;
        String year = String.valueOf(LocalDateTime.now().getYear());
        return String.format("RMA-%s-%04d", year, next);
    }

    private ReturnRequestDTO toDTOWithItems(ReturnRequest returnRequest) {
        ReturnRequestDTO dto = modelMapper.map(returnRequest, ReturnRequestDTO.class);
        List<ReturnItem> items = returnItemRepository.findByReturnRequestId(returnRequest.getId());
        List<ReturnItemDTO> itemDTOS = items.stream()
                .map(item -> modelMapper.map(item, ReturnItemDTO.class))
                .collect(Collectors.toList());
        dto.setItems(itemDTOS);
        return dto;
    }

    @Override
    @Transactional
    public Response createReturn(ReturnRequestDTO returnRequestDTO) {
        ReturnRequest returnRequest = modelMapper.map(returnRequestDTO, ReturnRequest.class);
        returnRequest.setReturnNumber(generateReturnNumber());
        returnRequest.setStatus("REQUESTED");
        returnRequest.setRequestDate(LocalDateTime.now());
        ReturnRequest saved = returnRequestRepository.save(returnRequest);

        if (returnRequestDTO.getItems() != null) {
            for (ReturnItemDTO itemDTO : returnRequestDTO.getItems()) {
                ReturnItem item = modelMapper.map(itemDTO, ReturnItem.class);
                item.setReturnRequestId(saved.getId());
                returnItemRepository.save(item);
            }
        }

        return Response.builder()
                .status(200)
                .message("Return request created successfully")
                .returnRequest(toDTOWithItems(saved))
                .build();
    }

    @Override
    public Response getAllReturns() {
        List<ReturnRequest> returnRequests = returnRequestRepository.findAllByOrderByCreatedAtDesc();
        List<ReturnRequestDTO> returnRequestDTOS = returnRequests.stream()
                .map(this::toDTOWithItems)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("success")
                .returnRequests(returnRequestDTOS)
                .build();
    }

    @Override
    public Response getReturnById(Long id) {
        ReturnRequest returnRequest = returnRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Return request not found"));
        ReturnRequestDTO dto = toDTOWithItems(returnRequest);

        return Response.builder()
                .status(200)
                .message("success")
                .returnRequest(dto)
                .build();
    }

    @Override
    @Transactional
    public Response updateStatus(Long id, String status) {
        ReturnRequest returnRequest = returnRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Return request not found"));

        returnRequest.setStatus(status);

        if ("RECEIVED".equalsIgnoreCase(status)) {
            returnRequest.setReceivedDate(LocalDateTime.now());
        }

        if ("COMPLETED".equalsIgnoreCase(status)) {
            returnRequest.setCompletedDate(LocalDateTime.now());
            List<ReturnItem> items = returnItemRepository.findByReturnRequestId(id);
            for (ReturnItem item : items) {
                if ("RESTOCK".equalsIgnoreCase(item.getDisposition())) {
                    Product product = productRepository.findById(item.getProductId())
                            .orElseThrow(() -> new NotFoundException("Product not found with id: " + item.getProductId()));
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        returnRequestRepository.save(returnRequest);

        ReturnRequestDTO dto = toDTOWithItems(returnRequest);

        return Response.builder()
                .status(200)
                .message("Return request status updated to " + status)
                .returnRequest(dto)
                .build();
    }

    @Override
    @Transactional
    public Response updateDisposition(Long returnId, Long itemId, String disposition) {
        ReturnItem item = returnItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Return item not found"));

        if (!item.getReturnRequestId().equals(returnId)) {
            throw new NotFoundException("Return item does not belong to this return request");
        }

        item.setDisposition(disposition);
        returnItemRepository.save(item);

        return Response.builder()
                .status(200)
                .message("Disposition updated to " + disposition)
                .build();
    }

    @Override
    @Transactional
    public Response deleteReturn(Long id) {
        returnRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Return request not found"));

        List<ReturnItem> items = returnItemRepository.findByReturnRequestId(id);
        returnItemRepository.deleteAll(items);
        returnRequestRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Return request deleted successfully")
                .build();
    }
}
