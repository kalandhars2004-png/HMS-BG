package com.phegondev.InventoryManagementSystem.purchasereturn;

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
public class PurchaseReturnServiceImpl implements PurchaseReturnService {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response createPurchaseReturn(PurchaseReturnDTO purchaseReturnDTO) {
        PurchaseReturn purchaseReturnToSave = modelMapper.map(purchaseReturnDTO, PurchaseReturn.class);
        purchaseReturnRepository.save(purchaseReturnToSave);

        return Response.builder()
                .status(200)
                .message("PurchaseReturn created successfully")
                .build();
    }

    @Override
    public Response getAllPurchaseReturns() {
        List<PurchaseReturn> purchaseReturns = purchaseReturnRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<PurchaseReturnDTO> purchaseReturnDTOS = modelMapper.map(purchaseReturns, new TypeToken<List<PurchaseReturnDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .purchaseReturns(purchaseReturnDTOS)
                .build();
    }

    @Override
    public Response getPurchaseReturnById(Long id) {
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PurchaseReturn Not Found"));
        PurchaseReturnDTO purchaseReturnDTO = modelMapper.map(purchaseReturn, PurchaseReturnDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .purchaseReturn(purchaseReturnDTO)
                .build();
    }

    @Override
    public Response updatePurchaseReturn(Long id, PurchaseReturnDTO purchaseReturnDTO) {
        PurchaseReturn existingPurchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PurchaseReturn Not Found"));

        if (purchaseReturnDTO.getProductId() != null) existingPurchaseReturn.setProductId(purchaseReturnDTO.getProductId());
        if (purchaseReturnDTO.getSupplierId() != null) existingPurchaseReturn.setSupplierId(purchaseReturnDTO.getSupplierId());
        if (purchaseReturnDTO.getQuantity() != null) existingPurchaseReturn.setQuantity(purchaseReturnDTO.getQuantity());
        if (purchaseReturnDTO.getReason() != null) existingPurchaseReturn.setReason(purchaseReturnDTO.getReason());
        if (purchaseReturnDTO.getReturnAmount() != null) existingPurchaseReturn.setReturnAmount(purchaseReturnDTO.getReturnAmount());
        if (purchaseReturnDTO.getStatus() != null) existingPurchaseReturn.setStatus(purchaseReturnDTO.getStatus());
        purchaseReturnRepository.save(existingPurchaseReturn);

        return Response.builder()
                .status(200)
                .message("PurchaseReturn Successfully Updated")
                .build();
    }

    @Override
    public Response deletePurchaseReturn(Long id) {
        purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("PurchaseReturn Not Found"));

        purchaseReturnRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("PurchaseReturn Successfully Deleted")
                .build();
    }
}
