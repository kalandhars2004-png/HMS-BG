package com.phegondev.InventoryManagementSystem.salesorder;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
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
        var t = TenantContext.get();
        Long branchId = t != null && t.branchId() != null ? t.branchId() : (salesOrder.getBranchId() != null ? salesOrder.getBranchId() : 1L);
        Long orgId = t != null && t.organizationId() != null ? t.organizationId() : 1L;
        if (t != null && !t.isSuperAdmin() && salesOrderDTO.getBranchId() != null && !salesOrderDTO.getBranchId().equals(t.branchId())) {
            throw new org.springframework.security.access.AccessDeniedException("Branch mismatch");
        }
        salesOrder.setBranchId(branchId);
        salesOrder.setOrganizationId(orgId);
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
        var t = TenantContext.get();
        List<SalesOrder> orders;
        if (t != null && t.isSuperAdmin() && t.branchId() == null) {
            orders = salesOrderRepository.findAllByOrderByCreatedAtDesc();
        } else {
            Long bid = t != null && t.branchId() != null ? t.branchId() : 1L;
            orders = salesOrderRepository.findByBranchIdOrderByCreatedAtDesc(bid);
            if (orders.isEmpty()) orders = salesOrderRepository.findAllByOrderByCreatedAtDesc();
        }
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
        var t = TenantContext.get();
        if (t != null && !t.isSuperAdmin() && salesOrder.getBranchId() != null && !salesOrder.getBranchId().equals(t.branchId())) {
            throw new org.springframework.security.access.AccessDeniedException("Branch mismatch");
        }
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

        // A repeated DELIVERED save used to deduct the same stock twice. Only
        // deduct when the order is actually transitioning INTO delivered.
        if ("DELIVERED".equals(status) && !"DELIVERED".equals(oldStatus)) {
            List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(id);
            for (SalesOrderItem item : items) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product Not Found"));
                int qty = item.getQuantity() != null ? item.getQuantity() : 0;
                int alreadyDelivered = item.getDeliveredQuantity() != null ? item.getDeliveredQuantity() : 0;
                int remaining = qty - alreadyDelivered;
                if (remaining > 0) {
                    if (product.getStockQuantity() < remaining) {
                        throw new com.phegondev.InventoryManagementSystem.exceptions.InsufficientStockException(
                                "Insufficient stock for " + product.getName() + ". Available: "
                                        + product.getStockQuantity() + ", Required: " + remaining);
                    }
                    product.setStockQuantity(product.getStockQuantity() - remaining);
                    item.setDeliveredQuantity(qty);
                    salesOrderItemRepository.save(item);
                    productRepository.save(product);
                }
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
        SalesOrder salesOrder = salesOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sales Order Not Found"));

        // Deleting a delivered order used to leave stock permanently deducted.
        // Put the delivered quantity back before removing the record.
        if ("DELIVERED".equals(salesOrder.getStatus())) {
            List<SalesOrderItem> items = salesOrderItemRepository.findBySalesOrderId(id);
            for (SalesOrderItem item : items) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product Not Found"));
                int delivered = item.getDeliveredQuantity() != null ? item.getDeliveredQuantity() : 0;
                if (delivered > 0) {
                    product.setStockQuantity(product.getStockQuantity() + delivered);
                    productRepository.save(product);
                }
            }
        }

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
        // max(id)+1 instead of count()+1: deleting a row no longer reuses an
        // already-issued number.
        Long maxId = salesOrderRepository.findMaxId();
        long next = (maxId != null ? maxId : 0) + 1;
        return "SO-" + year + "-" + String.format("%04d", next);
    }
}
