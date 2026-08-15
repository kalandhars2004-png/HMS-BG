package com.phegondev.InventoryManagementSystem.equipment;


import com.phegondev.InventoryManagementSystem.equipment.EquipmentDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.equipment.Equipment;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.equipment.EquipmentRepository;
import com.phegondev.InventoryManagementSystem.equipment.EquipmentService;
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
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response createEquipment(EquipmentDTO equipmentDTO) {
        Equipment equipmentToSave = modelMapper.map(equipmentDTO, Equipment.class);
        equipmentRepository.save(equipmentToSave);

        return Response.builder()
                .status(200)
                .message("Equipment created successfully")
                .build();
    }

    @Override
    public Response getAllEquipments() {

        List<Equipment> equipments = equipmentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<EquipmentDTO> equipmentDTOS = modelMapper.map(equipments, new TypeToken<List<EquipmentDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .equipments(equipmentDTOS)
                .build();
    }

    @Override
    public Response getEquipmentById(Long id) {

        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Equipment Not Found"));
        EquipmentDTO equipmentDTO = modelMapper.map(equipment, EquipmentDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .equipment(equipmentDTO)
                .build();
    }

    @Override
    public Response updateEquipment(Long id, EquipmentDTO equipmentDTO) {

        Equipment existingEquipment = equipmentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Equipment Not Found"));

        if (equipmentDTO.getName() != null) existingEquipment.setName(equipmentDTO.getName());
        if (equipmentDTO.getModel() != null) existingEquipment.setModel(equipmentDTO.getModel());
        if (equipmentDTO.getCategory() != null) existingEquipment.setCategory(equipmentDTO.getCategory());
        if (equipmentDTO.getSerialNo() != null) existingEquipment.setSerialNo(equipmentDTO.getSerialNo());
        if (equipmentDTO.getStatus() != null) existingEquipment.setStatus(equipmentDTO.getStatus());
        if (equipmentDTO.getLastService() != null) existingEquipment.setLastService(equipmentDTO.getLastService());
        if (equipmentDTO.getNextService() != null) existingEquipment.setNextService(equipmentDTO.getNextService());
        if (equipmentDTO.getLocation() != null) existingEquipment.setLocation(equipmentDTO.getLocation());
        equipmentRepository.save(existingEquipment);

        return Response.builder()
                .status(200)
                .message("Equipment Successfully Updated")
                .build();

    }

    @Override
    public Response deleteEquipment(Long id) {

         equipmentRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Equipment Not Found"));

        equipmentRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Equipment Successfully Deleted")
                .build();
    }
}
