package com.phegondev.InventoryManagementSystem.brand;


import com.phegondev.InventoryManagementSystem.brand.BrandDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.brand.Brand;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.brand.BrandRepository;
import com.phegondev.InventoryManagementSystem.brand.BrandService;
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
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response createBrand(BrandDTO brandDTO) {
        Brand brandToSave = modelMapper.map(brandDTO, Brand.class);
        brandRepository.save(brandToSave);

        return Response.builder()
                .status(200)
                .message("Brand created successfully")
                .build();
    }

    @Override
    public Response getAllBrands() {

        List<Brand> brands = brandRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<BrandDTO> brandDTOS = modelMapper.map(brands, new TypeToken<List<BrandDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .brands(brandDTOS)
                .build();
    }

    @Override
    public Response getBrandById(Long id) {

        Brand brand = brandRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Brand Not Found"));
        BrandDTO brandDTO = modelMapper.map(brand, BrandDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .brand(brandDTO)
                .build();
    }

    @Override
    public Response updateBrand(Long id, BrandDTO brandDTO) {

        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Brand Not Found"));

        if (brandDTO.getName() != null) existingBrand.setName(brandDTO.getName());
        if (brandDTO.getDescription() != null) existingBrand.setDescription(brandDTO.getDescription());
        if (brandDTO.getStatus() != null) existingBrand.setStatus(brandDTO.getStatus());
        brandRepository.save(existingBrand);

        return Response.builder()
                .status(200)
                .message("Brand Successfully Updated")
                .build();

    }

    @Override
    public Response deleteBrand(Long id) {

         brandRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Brand Not Found"));

        brandRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Brand Successfully Deleted")
                .build();
    }
}
