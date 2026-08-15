package com.phegondev.InventoryManagementSystem.salesorder;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public Response createSalesOrder(SalesOrderDTO salesOrderDTO) {
        SalesOrder salesOrder = modelMapper.map(salesOrderDTO, SalesOrder.class);
        salesOrder.setSoNumber(generateSONumber());
        salesOrder.setCreatedAt(LocalDateTime.now());
        SalesOrder saved = salesOrderRepository.save(salesOrder);

        if (salesOrderDTO.getItems() != null) {
            for (SalesOrderItemDTO itemDTO : salesOrderDTO.getItems()) {
                SalesOrderItem item = modelMapper.map(itemDTO, SalesOrderItem.class);
                item.setSalesOrderId(saved.getId());
                salesOrderItemRepository.save(item);
            }
        }

        return Response.builder()
                .status(200)
                .message("Sales Order created successfully")
                .salesOrder(mapOrderWithItems(saved))
                .build();
    }

    @Override
    public Response getAllSalesOrders() {
        List<SalesOrder> orders = salesOrderRepository.findAllByOrderByCreatedAtDesc();
        List<SalesOrderDTO> orderDTOS = orders.stream()
                .map(this::mapOrderWithItems)
                .collect(Collectors.toList());

        return Response.builder()
                .status(200)
                .message("success")
                .salesOrders(orderDTOS)
                .build();
    }

    @Override
    public Response getSalesOrderById(Long id) {
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sales Order Not Found"));
        SalesOrderDTO dto = mapOrderWithItems(salesOrder);

        return Response.builder()
                .status(200)
                .message("success")
                .salesOrder(dto)
                .build();
    }

    @Override
    @Transactional
    public Response updateSalesOrderStatus(Long id, String status) {
        SalesOrder existing = salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sales Order Not Found"));

        String oldStatus = existing.getStatus();
        existing.setStatus(status);

        if ("DELIVERED".equals(status)) {
            List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(id);
            for (SalesOrderItem item : items) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product Not Found"));
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                item.setDeliveredQuantity(item.getQuantity());
                salesOrderItemRepository.save(item);
                productRepository.save(product);
            }
        }

        if ("CANCELLED".equals(status) && "DELIVERED".equals(oldStatus)) {
            List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(id);
            for (SalesOrderItem item : items) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product Not Found"));
                product.setStockQuantity(product.getStockQuantity() + (item.getDeliveredQuantity() != null ? item.getDeliveredQuantity() : 0));
                item.setDeliveredQuantity(0);
                salesOrderItemRepository.save(item);
                productRepository.save(product);
            }
        }

        salesOrderRepository.save(existing);

        return Response.builder()
                .status(200)
                .message("Sales Order status updated successfully")
                .build();
    }

    @Override
    @Transactional
    public Response deleteSalesOrder(Long id) {
        salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sales Order Not Found"));

        List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(id);
        salesOrderItemRepository.deleteAll(items);
        salesOrderRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Sales Order Successfully Deleted")
                .build();
    }

    @Override
    @Transactional
    public Response updatePaymentStatus(Long id, String paymentStatus) {
        SalesOrder existing = salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sales Order Not Found"));
        existing.setPaymentStatus(paymentStatus);
        salesOrderRepository.save(existing);

        return Response.builder()
                .status(200)
                .message("Payment status updated successfully")
                .build();
    }

    private SalesOrderDTO mapOrderWithItems(SalesOrder order) {
        SalesOrderDTO dto = modelMapper.map(order, SalesOrderDTO.class);
        List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(order.getId());
        List<SalesOrderItemDTO> itemDTOS = items.stream().map(item -> {
            SalesOrderItemDTO itemDTO = modelMapper.map(item, SalesOrderItemDTO.class);
            productRepository.findById(item.getProductId()).ifPresent(p -> itemDTO.setProductName(p.getName()));
            return itemDTO;
        }).collect(Collectors.toList());
        dto.setItems(itemDTOS);
        return dto;
    }

    private String generateSONumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count = salesOrderRepository.count() + 1;
        return "SO-" + year + "-" + String.format("%04d", count);
    }
}
