package com.phegondev.InventoryManagementSystem.unit;


import com.phegondev.InventoryManagementSystem.unit.UnitDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.unit.Unit;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.unit.UnitRepository;
import com.phegondev.InventoryManagementSystem.unit.UnitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final ModelMapper modelMapper;

    @Override
    @CacheEvict(cacheNames = "units", allEntries = true)
    public Response createUnit(UnitDTO unitDTO) {
        Optional<Unit> existing = unitRepository.findByNameIgnoreCase(unitDTO.getName());
        if (existing.isPresent()) {
            return Response.builder()
                    .status(400)
                    .message("Unit already exists with name: " + unitDTO.getName())
                    .build();
        }
        if (unitDTO.getShortName() != null) {
            Optional<Unit> shortExists = unitRepository.findByShortNameIgnoreCase(unitDTO.getShortName());
            if (shortExists.isPresent()) {
                return Response.builder()
                        .status(400)
                        .message("Short name already in use: " + unitDTO.getShortName())
                        .build();
            }
        }
        Unit unitToSave = modelMapper.map(unitDTO, Unit.class);
        if (unitToSave.getStatus() == null) {
            unitToSave.setStatus(true);
        }
        if (unitToSave.getArchived() == null) {
            unitToSave.setArchived(false);
        }
        if (unitToSave.getUsageCount() == null) {
            unitToSave.setUsageCount(0);
        }
        if (unitToSave.getCreatedAt() == null) {
            unitToSave.setCreatedAt(LocalDateTime.now());
        }
        unitToSave.setUpdatedAt(LocalDateTime.now());
        if (Boolean.TRUE.equals(unitToSave.getIsDefault())) {
            unsetOtherDefaults(unitToSave.getId());
        }
        unitRepository.save(unitToSave);

        return Response.builder()
                .status(200)
                .message("Unit created successfully")
                .build();
    }

    @Override
    @Cacheable(cacheNames = "units", key = "'all'")
    public Response getAllUnits() {

        List<Unit> units = unitRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        List<UnitDTO> unitDTOS = modelMapper.map(units, new TypeToken<List<UnitDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .units(unitDTOS)
                .build();
    }

    @Override
    public Response filterUnits(UnitFilter filter) {
        int page = filter.getPage() > 0 ? filter.getPage() - 1 : 0;
        int size = filter.getLimit() > 0 ? Math.min(filter.getLimit(), 200) : 20;

        Specification<Unit> spec = UnitSpecification.buildCriteria(filter);
        Sort sort = UnitSpecification.buildSort(filter.getSort(), filter.getDirection());
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Unit> result = unitRepository.findAll(spec, pageable);

        List<UnitDTO> unitDTOS = modelMapper.map(result.getContent(),
                new TypeToken<List<UnitDTO>>() {}.getType());

        return Response.builder()
                .status(200)
                .message("success")
                .units(unitDTOS)
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .currentPage(filter.getPage())
                .pageSize(result.getSize())
                .build();
    }

    @Override
    public Response getUnitById(Long id) {

        Unit unit = unitRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Unit Not Found"));
        UnitDTO unitDTO = modelMapper.map(unit, UnitDTO.class);

        return Response.builder()
                .status(200)
                .message("success")
                .unit(unitDTO)
                .build();
    }

    @Override
    @CacheEvict(cacheNames = "units", allEntries = true)
    public Response updateUnit(Long id, UnitDTO unitDTO) {

        Unit existingUnit = unitRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Unit Not Found"));

        if (unitDTO.getName() != null) existingUnit.setName(unitDTO.getName());
        if (unitDTO.getShortName() != null) existingUnit.setShortName(unitDTO.getShortName());
        if (unitDTO.getPluralName() != null) existingUnit.setPluralName(unitDTO.getPluralName());
        if (unitDTO.getSymbol() != null) existingUnit.setSymbol(unitDTO.getSymbol());
        if (unitDTO.getDescription() != null) existingUnit.setDescription(unitDTO.getDescription());
        if (unitDTO.getCategory() != null) existingUnit.setCategory(unitDTO.getCategory());
        if (unitDTO.getUnitType() != null) existingUnit.setUnitType(unitDTO.getUnitType());
        if (unitDTO.getMeasurementSystem() != null) existingUnit.setMeasurementSystem(unitDTO.getMeasurementSystem());
        if (unitDTO.getBaseUnit() != null) existingUnit.setBaseUnit(unitDTO.getBaseUnit());
        if (unitDTO.getConversionValue() != null) existingUnit.setConversionValue(unitDTO.getConversionValue());
        if (unitDTO.getConversionRate() != null) existingUnit.setConversionRate(unitDTO.getConversionRate());
        if (unitDTO.getDecimalPlaces() != null) existingUnit.setDecimalPlaces(unitDTO.getDecimalPlaces());
        if (unitDTO.getDisplayFormat() != null) existingUnit.setDisplayFormat(unitDTO.getDisplayFormat());
        if (unitDTO.getApplicableFor() != null) existingUnit.setApplicableFor(unitDTO.getApplicableFor());
        if (unitDTO.getIsDefault() != null) existingUnit.setIsDefault(unitDTO.getIsDefault());
        if (unitDTO.getUsesPurchases() != null) existingUnit.setUsesPurchases(unitDTO.getUsesPurchases());
        if (unitDTO.getUsesSales() != null) existingUnit.setUsesSales(unitDTO.getUsesSales());
        if (unitDTO.getUsesInventory() != null) existingUnit.setUsesInventory(unitDTO.getUsesInventory());
        if (unitDTO.getUsesManufacturing() != null) existingUnit.setUsesManufacturing(unitDTO.getUsesManufacturing());
        if (unitDTO.getUsesPrescriptions() != null) existingUnit.setUsesPrescriptions(unitDTO.getUsesPrescriptions());
        if (unitDTO.getUsesReports() != null) existingUnit.setUsesReports(unitDTO.getUsesReports());
        if (unitDTO.getUsesBarcodePrinting() != null) existingUnit.setUsesBarcodePrinting(unitDTO.getUsesBarcodePrinting());
        if (unitDTO.getStatus() != null) existingUnit.setStatus(unitDTO.getStatus());
        if (Boolean.TRUE.equals(existingUnit.getIsDefault())) {
            unsetOtherDefaults(existingUnit.getId());
        }
        existingUnit.setUpdatedAt(LocalDateTime.now());
        unitRepository.save(existingUnit);

        return Response.builder()
                .status(200)
                .message("Unit Successfully Updated")
                .build();

    }

    @Override
    @CacheEvict(cacheNames = "units", allEntries = true)
    public Response deleteUnit(Long id) {

         unitRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Unit Not Found"));

        unitRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Unit Successfully Deleted")
                .build();
    }

    private void unsetOtherDefaults(Long exceptId) {
        unitRepository.findAll().stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsDefault()))
                .filter(u -> !u.getId().equals(exceptId))
                .forEach(u -> {
                    u.setIsDefault(false);
                    unitRepository.save(u);
                });
    }
}
