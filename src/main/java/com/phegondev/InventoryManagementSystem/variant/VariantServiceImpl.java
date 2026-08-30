package com.phegondev.InventoryManagementSystem.variant;


import com.phegondev.InventoryManagementSystem.variant.VariantDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.variant.Variant;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.variant.VariantRepository;
import com.phegondev.InventoryManagementSystem.variant.VariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VariantServiceImpl implements VariantService {

    private final VariantRepository variantRepository;
    private final ModelMapper modelMapper;

    @Override
    @CacheEvict(cacheNames = "variants", allEntries = true)
    public Response createVariant(VariantDTO variantDTO) {
        Variant variantToSave = modelMapper.map(variantDTO, Variant.class);
        variantRepository.save(variantToSave);

        return Response.builder()
                .status(200)
                .message("Variant created successfully")
                .build();
    }

    @Override
    @Cacheable(cacheNames = "variants", key = "'all'")
    public Response getAllVariants() {

        List<Variant> variants = variantRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<VariantDTO> variantDTOS = modelMapper.map(variants, new TypeToken<List<VariantDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .variants(variantDTOS)
                .build();
    }

    @Override
    public Response getVariantById(Long id) {

        Variant variant = variantRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Variant Not Found"));
        VariantDTO variantDTO = modelMapper.map(variant, VariantDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .variant(variantDTO)
                .build();
    }

    @Override
    @CacheEvict(cacheNames = "variants", allEntries = true)
    public Response updateVariant(Long id, VariantDTO variantDTO) {

        Variant existingVariant = variantRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Variant Not Found"));

        if (variantDTO.getName() != null) existingVariant.setName(variantDTO.getName());
        if (variantDTO.getAttributeName() != null) existingVariant.setAttributeName(variantDTO.getAttributeName());
        if (variantDTO.getDescription() != null) existingVariant.setDescription(variantDTO.getDescription());
        variantRepository.save(existingVariant);

        return Response.builder()
                .status(200)
                .message("Variant Successfully Updated")
                .build();

    }

    @Override
    @CacheEvict(cacheNames = "variants", allEntries = true)
    public Response deleteVariant(Long id) {

         variantRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Variant Not Found"));

        variantRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Variant Successfully Deleted")
                .build();
    }
}

