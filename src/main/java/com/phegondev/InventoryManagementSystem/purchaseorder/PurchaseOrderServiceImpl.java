package com.phegondev.InventoryManagementSystem.purchaseorder;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.supplier.Supplier;
import com.phegondev.InventoryManagementSystem.supplier.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ModelMapper modelMapper;

    private final AtomicInteger poCounter = new AtomicInteger(0);

    private String generatePONumber() {
        int count = poCounter.incrementAndGet();
        return String.format("PO-2026-%04d", count);
    }

    @Override
    @Transactional
    public Response addPurchaseOrder(PurchaseOrderDTO purchaseOrderDTO) {
        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .poNumber(generatePONumber())
                .supplierId(purchaseOrderDTO.getSupplierId())
                .totalAmount(purchaseOrderDTO.getTotalAmount())
                .status("DRAFT")
                .paymentTerms(purchaseOrderDTO.getPaymentTerms())
                .orderDate(LocalDateTime.now())
                .expectedDelivery(purchaseOrderDTO.getExpectedDelivery())
                .createdAt(LocalDateTime.now())
                .build();

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);

        if (purchaseOrderDTO.getItems() != null) {
            for (PurchaseOrderItemDTO itemDTO : purchaseOrderDTO.getItems()) {
                PurchaseOrderItem item = PurchaseOrderItem.builder()
                        .purchaseOrderId(saved.getId())
                        .productId(itemDTO.getProductId())
                        .quantity(itemDTO.getQuantity())
                        .unitPrice(itemDTO.getUnitPrice())
                        .totalPrice(itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())))
                        .receivedQuantity(0)
                        .build();
                purchaseOrderItemRepository.save(item);
            }
        }

        PurchaseOrderDTO resultDto = modelMapper.map(saved, PurchaseOrderDTO.class);
        Supplier supplier = supplierRepository.findById(saved.getSupplierId()).orElse(null);
        if (supplier != null) resultDto.setSupplierName(supplier.getName());

        return Response.builder()
                .status(200)
                .message("Purchase order created successfully")
                .purchaseOrder(resultDto)
                .build();
    }

    @Override
    public Response getAllPurchaseOrders() {
        List<PurchaseOrder> orders = purchaseOrderRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<PurchaseOrderDTO> dtos = orders.stream().map(order -> {
            PurchaseOrderDTO dto = modelMapper.map(order, PurchaseOrderDTO.class);
            Supplier supplier = supplierRepository.findById(order.getSupplierId()).orElse(null);
            if (supplier != null) dto.setSupplierName(supplier.getName());
            List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(order.getId());
            dto.setItems(modelMapper.map(items, new TypeToken<List<PurchaseOrderItemDTO>>() {}.getType()));
            return dto;
        }).toList();

        return Response.builder()
                .status(200)
                .message("success")
                .purchaseOrders(dtos)
                .build();
    }

    @Override
    public Response getPurchaseOrderById(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase Order Not Found"));

        PurchaseOrderDTO dto = modelMapper.map(order, PurchaseOrderDTO.class);
        Supplier supplier = supplierRepository.findById(order.getSupplierId()).orElse(null);
        if (supplier != null) dto.setSupplierName(supplier.getName());

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(order.getId());
        List<PurchaseOrderItemDTO> itemDTOs = items.stream().map(item -> {
            PurchaseOrderItemDTO itemDTO = modelMapper.map(item, PurchaseOrderItemDTO.class);
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) itemDTO.setProductName(product.getName());
            return itemDTO;
        }).toList();
        dto.setItems(itemDTOs);

        return Response.builder()
                .status(200)
                .message("success")
                .purchaseOrder(dto)
                .build();
    }

    @Override
    public Response updateStatus(Long id, String status) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase Order Not Found"));

        order.setStatus(status);
        purchaseOrderRepository.save(order);

        PurchaseOrderDTO dto = modelMapper.map(order, PurchaseOrderDTO.class);
        Supplier supplier = supplierRepository.findById(order.getSupplierId()).orElse(null);
        if (supplier != null) dto.setSupplierName(supplier.getName());

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(order.getId());
        List<PurchaseOrderItemDTO> itemDTOs = items.stream().map(item -> {
            PurchaseOrderItemDTO itemDTO = modelMapper.map(item, PurchaseOrderItemDTO.class);
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) itemDTO.setProductName(product.getName());
            return itemDTO;
        }).toList();
        dto.setItems(itemDTOs);

        return Response.builder()
                .status(200)
                .message("Purchase order status updated to " + status)
                .purchaseOrder(dto)
                .build();
    }

    @Override
    @Transactional
    public Response receiveItems(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase Order Not Found"));

        List<PurchaseOrderItem> items = purchaseOrderItemRepository.findByPurchaseOrderId(id);

        for (PurchaseOrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product Not Found"));

            int receivedNow = item.getQuantity() - item.getReceivedQuantity();
            product.setStockQuantity(product.getStockQuantity() + receivedNow);
            item.setReceivedQuantity(item.getQuantity());
            productRepository.save(product);
            purchaseOrderItemRepository.save(item);
        }

        order.setStatus("RECEIVED");
        purchaseOrderRepository.save(order);

        return Response.builder()
                .status(200)
                .message("Items received successfully")
                .build();
    }

    @Override
    @Transactional
    public Response deletePurchaseOrder(Long id) {
        purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Purchase Order Not Found"));

        purchaseOrderItemRepository.deleteByPurchaseOrderId(id);
        purchaseOrderRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Purchase order deleted successfully")
                .build();
    }
}
