package com.phegondev.InventoryManagementSystem.rack;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RackServiceImpl implements RackService {

    private final RackRepository rackRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response createRack(RackDTO rackDTO) {
        Optional<Rack> existing = rackRepository.findByCodeIgnoreCase(rackDTO.getCode());
        if (existing.isPresent()) {
            return Response.builder()
                    .status(400)
                    .message("Rack already exists with code: " + rackDTO.getCode())
                    .build();
        }
        Rack rackToSave = modelMapper.map(rackDTO, Rack.class);
        if (rackToSave.getStatus() == null) {
            rackToSave.setStatus("ACTIVE");
        }
        if (rackToSave.getAssignedMedicines() == null) {
            rackToSave.setAssignedMedicines(0);
        }
        if (rackToSave.getCreatedAt() == null) {
            rackToSave.setCreatedAt(LocalDateTime.now());
        }
        rackToSave.setUpdatedAt(LocalDateTime.now());
        rackRepository.save(rackToSave);

        return Response.builder()
                .status(200)
                .message("Rack created successfully")
                .build();
    }

    @Override
    public Response getAllRacks() {
        List<Rack> racks = rackRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<RackDTO> rackDTOS = modelMapper.map(racks, new TypeToken<List<RackDTO>>() {}.getType());
        rackDTOS.forEach(this::fillCapacity);
        return Response.builder()
                .status(200)
                .message("success")
                .racks(rackDTOS)
                .build();
    }

    @Override
    public Response getRackById(Long id) {
        Rack rack = rackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rack Not Found"));
        RackDTO rackDTO = modelMapper.map(rack, RackDTO.class);
        fillCapacity(rackDTO);
        return Response.builder()
                .status(200)
                .message("success")
                .rack(rackDTO)
                .build();
    }

    @Override
    public Response updateRack(Long id, RackDTO rackDTO) {
        Rack existingRack = rackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rack Not Found"));

        if (rackDTO.getCode() != null && !rackDTO.getCode().equalsIgnoreCase(existingRack.getCode())) {
            Optional<Rack> codeExists = rackRepository.findByCodeIgnoreCase(rackDTO.getCode());
            if (codeExists.isPresent()) {
                return Response.builder()
                        .status(400)
                        .message("Rack already exists with code: " + rackDTO.getCode())
                        .build();
            }
        }
        if (rackDTO.getCode() != null) existingRack.setCode(rackDTO.getCode());
        if (rackDTO.getCategory() != null) existingRack.setCategory(rackDTO.getCategory());
        if (rackDTO.getRowsCount() != null) existingRack.setRowsCount(rackDTO.getRowsCount());
        if (rackDTO.getColumns() != null) existingRack.setColumns(rackDTO.getColumns());
        if (rackDTO.getBins() != null) existingRack.setBins(rackDTO.getBins());
        if (rackDTO.getAlertThreshold() != null) existingRack.setAlertThreshold(rackDTO.getAlertThreshold());
        if (rackDTO.getTemperature() != null) existingRack.setTemperature(rackDTO.getTemperature());
        if (rackDTO.getStatus() != null) existingRack.setStatus(rackDTO.getStatus());
        if (rackDTO.getAssignedMedicines() != null) existingRack.setAssignedMedicines(rackDTO.getAssignedMedicines());
        existingRack.setUpdatedAt(LocalDateTime.now());
        rackRepository.save(existingRack);

        return Response.builder()
                .status(200)
                .message("Rack Successfully Updated")
                .build();
    }

    @Override
    public Response deleteRack(Long id) {
        rackRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rack Not Found"));
        rackRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Rack Successfully Deleted")
                .build();
    }

    private void fillCapacity(RackDTO dto) {
        int bins = dto.getBins() != null && dto.getBins() > 0 ? dto.getBins() : 0;
        int assigned = dto.getAssignedMedicines() != null ? dto.getAssignedMedicines() : 0;
        dto.setCapacityPercent(bins > 0 ? Math.min(100, Math.round(assigned * 100f / bins)) : 0);
    }
}
