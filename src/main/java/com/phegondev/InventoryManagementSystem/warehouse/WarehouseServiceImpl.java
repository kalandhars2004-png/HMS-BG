package com.phegondev.InventoryManagementSystem.warehouse;


import com.phegondev.InventoryManagementSystem.warehouse.WarehouseDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.warehouse.Warehouse;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.warehouse.WarehouseRepository;
import com.phegondev.InventoryManagementSystem.warehouse.WarehouseService;
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
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response createWarehouse(WarehouseDTO warehouseDTO) {
        Warehouse warehouseToSave = modelMapper.map(warehouseDTO, Warehouse.class);
        warehouseRepository.save(warehouseToSave);

        return Response.builder()
                .status(200)
                .message("Warehouse created successfully")
                .build();
    }

    @Override
    public Response getAllWarehouses() {

        List<Warehouse> warehouses = warehouseRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<WarehouseDTO> warehouseDTOS = modelMapper.map(warehouses, new TypeToken<List<WarehouseDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .warehouses(warehouseDTOS)
                .build();
    }

    @Override
    public Response getWarehouseById(Long id) {

        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Warehouse Not Found"));
        WarehouseDTO warehouseDTO = modelMapper.map(warehouse, WarehouseDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .warehouse(warehouseDTO)
                .build();
    }

    @Override
    public Response updateWarehouse(Long id, WarehouseDTO warehouseDTO) {

        Warehouse existingWarehouse = warehouseRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Warehouse Not Found"));

        if (warehouseDTO.getWarehouse() != null) existingWarehouse.setWarehouse(warehouseDTO.getWarehouse());
        if (warehouseDTO.getContactPerson() != null) existingWarehouse.setContactPerson(warehouseDTO.getContactPerson());
        if (warehouseDTO.getEmail() != null) existingWarehouse.setEmail(warehouseDTO.getEmail());
        if (warehouseDTO.getPhone() != null) existingWarehouse.setPhone(warehouseDTO.getPhone());
        if (warehouseDTO.getAddress() != null) existingWarehouse.setAddress(warehouseDTO.getAddress());
        if (warehouseDTO.getCity() != null) existingWarehouse.setCity(warehouseDTO.getCity());
        if (warehouseDTO.getState() != null) existingWarehouse.setState(warehouseDTO.getState());
        if (warehouseDTO.getCountry() != null) existingWarehouse.setCountry(warehouseDTO.getCountry());
        if (warehouseDTO.getPostalCode() != null) existingWarehouse.setPostalCode(warehouseDTO.getPostalCode());
        if (warehouseDTO.getTotalProducts() != null) existingWarehouse.setTotalProducts(warehouseDTO.getTotalProducts());
        if (warehouseDTO.getStock() != null) existingWarehouse.setStock(warehouseDTO.getStock());
        if (warehouseDTO.getQty() != null) existingWarehouse.setQty(warehouseDTO.getQty());
        if (warehouseDTO.getStatus() != null) existingWarehouse.setStatus(warehouseDTO.getStatus());
        warehouseRepository.save(existingWarehouse);

        return Response.builder()
                .status(200)
                .message("Warehouse Successfully Updated")
                .build();

    }

    @Override
    public Response deleteWarehouse(Long id) {

         warehouseRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Warehouse Not Found"));

        warehouseRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Warehouse Successfully Deleted")
                .build();
    }
}
