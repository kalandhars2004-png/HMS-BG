package com.phegondev.InventoryManagementSystem.kitting;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitServiceImpl implements KitService {

    private final KitRepository kitRepository;
    private final KitItemRepository kitItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    private final AtomicInteger kitCounter = new AtomicInteger(0);

    private String generateKitSku() {
        int count = kitCounter.incrementAndGet();
        return String.format("KIT-%03d", count);
    }

    @Override
    @Transactional
    public Response addKit(KitDTO kitDTO) {
        Kit kit = Kit.builder()
                .name(kitDTO.getName())
                .sku(generateKitSku())
                .description(kitDTO.getDescription())
                .sellingPrice(kitDTO.getSellingPrice())
                .costPrice(kitDTO.getCostPrice())
                .quantityOnHand(0)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();

        Kit saved = kitRepository.save(kit);

        if (kitDTO.getItems() != null) {
            for (KitItemDTO itemDTO : kitDTO.getItems()) {
                KitItem item = KitItem.builder()
                        .kitId(saved.getId())
                        .productId(itemDTO.getProductId())
                        .quantity(itemDTO.getQuantity())
                        .build();
                kitItemRepository.save(item);
            }
        }

        KitDTO resultDto = modelMapper.map(saved, KitDTO.class);
        resultDto.setItems(kitDTO.getItems());

        return Response.builder()
                .status(200)
                .message("Kit created successfully")
                .kit(resultDto)
                .build();
    }

    @Override
    public Response getAllKits() {
        List<Kit> kits = kitRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<KitDTO> dtos = kits.stream().map(kit -> {
            KitDTO dto = modelMapper.map(kit, KitDTO.class);
            List<KitItem> items = kitItemRepository.findByKitId(kit.getId());
            dto.setItems(items.stream().map(item -> {
                KitItemDTO itemDTO = modelMapper.map(item, KitItemDTO.class);
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) itemDTO.setProductName(product.getName());
                return itemDTO;
            }).toList());
            return dto;
        }).toList();

        return Response.builder()
                .status(200)
                .message("success")
                .kits(dtos)
                .build();
    }

    @Override
    public Response getKitById(Long id) {
        Kit kit = kitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kit Not Found"));

        KitDTO dto = modelMapper.map(kit, KitDTO.class);
        List<KitItem> items = kitItemRepository.findByKitId(id);
        dto.setItems(items.stream().map(item -> {
            KitItemDTO itemDTO = modelMapper.map(item, KitItemDTO.class);
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product != null) itemDTO.setProductName(product.getName());
            return itemDTO;
        }).toList());

        return Response.builder()
                .status(200)
                .message("success")
                .kit(dto)
                .build();
    }

    @Override
    @Transactional
    public Response updateKit(Long id, KitDTO kitDTO) {
        Kit kit = kitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kit Not Found"));

        if (kitDTO.getName() != null) kit.setName(kitDTO.getName());
        if (kitDTO.getDescription() != null) kit.setDescription(kitDTO.getDescription());
        if (kitDTO.getSellingPrice() != null) kit.setSellingPrice(kitDTO.getSellingPrice());
        if (kitDTO.getCostPrice() != null) kit.setCostPrice(kitDTO.getCostPrice());
        if (kitDTO.getStatus() != null) kit.setStatus(kitDTO.getStatus());

        Kit saved = kitRepository.save(kit);

        if (kitDTO.getItems() != null) {
            kitItemRepository.deleteByKitId(id);
            for (KitItemDTO itemDTO : kitDTO.getItems()) {
                KitItem item = KitItem.builder()
                        .kitId(id)
                        .productId(itemDTO.getProductId())
                        .quantity(itemDTO.getQuantity())
                        .build();
                kitItemRepository.save(item);
            }
        }

        KitDTO updatedDto = modelMapper.map(saved, KitDTO.class);
        updatedDto.setItems(kitDTO.getItems());

        return Response.builder()
                .status(200)
                .message("Kit updated successfully")
                .kit(updatedDto)
                .build();
    }

    @Override
    @Transactional
    public Response deleteKit(Long id) {
        kitRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Kit Not Found"));

        kitItemRepository.deleteByKitId(id);
        kitRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Kit deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public Response assembleKit(Long kitId, Integer quantity) {
        Kit kit = kitRepository.findById(kitId)
                .orElseThrow(() -> new NotFoundException("Kit Not Found"));

        List<KitItem> items = kitItemRepository.findByKitId(kitId);

        for (KitItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product Not Found"));

            int requiredQty = item.getQuantity() * quantity;
            if (product.getStockQuantity() < requiredQty) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - requiredQty);
            productRepository.save(product);
        }

        kit.setQuantityOnHand(kit.getQuantityOnHand() + quantity);
        kitRepository.save(kit);

        return Response.builder()
                .status(200)
                .message("Kit assembled successfully")
                .build();
    }

    @Override
    @Transactional
    public Response disassembleKit(Long kitId, Integer quantity) {
        Kit kit = kitRepository.findById(kitId)
                .orElseThrow(() -> new NotFoundException("Kit Not Found"));

        if (kit.getQuantityOnHand() < quantity) {
            throw new IllegalStateException("Not enough kits available to disassemble");
        }

        List<KitItem> items = kitItemRepository.findByKitId(kitId);

        for (KitItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product Not Found"));

            int returnQty = item.getQuantity() * quantity;
            product.setStockQuantity(product.getStockQuantity() + returnQty);
            productRepository.save(product);
        }

        kit.setQuantityOnHand(kit.getQuantityOnHand() - quantity);
        kitRepository.save(kit);

        return Response.builder()
                .status(200)
                .message("Kit disassembled successfully")
                .build();
    }
}
