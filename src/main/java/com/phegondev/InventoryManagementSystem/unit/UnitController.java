package com.phegondev.InventoryManagementSystem.unit;

import com.phegondev.InventoryManagementSystem.unit.UnitDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.unit.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;


    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> createUnit(@RequestBody @Valid UnitDTO unitDTO) {
        return ResponseEntity.ok(unitService.createUnit(unitDTO));
    }

    @GetMapping("/all")
    public ResponseEntity<Response> getAllUnits() {
        return ResponseEntity.ok(unitService.getAllUnits());
    }

    @GetMapping
    public ResponseEntity<Response> filterUnits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String measurementSystem,
            @RequestParam(required = false) String baseUnit,
            @RequestParam(required = false) List<String> usage,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) LocalDate updatedFromDate,
            @RequestParam(required = false) LocalDate updatedToDate,
            @RequestParam(required = false) BigDecimal minConversionRate,
            @RequestParam(required = false) BigDecimal maxConversionRate,
            @RequestParam(required = false) String createdBy) {

        UnitFilter filter = UnitFilter.builder()
                .page(page)
                .limit(limit)
                .search(search)
                .status(status)
                .category(category)
                .measurementSystem(measurementSystem)
                .baseUnit(baseUnit)
                .usages(usage)
                .sort(sort)
                .direction(direction)
                .fromDate(fromDate)
                .toDate(toDate)
                .updatedFromDate(updatedFromDate)
                .updatedToDate(updatedToDate)
                .minConversionRate(minConversionRate)
                .maxConversionRate(maxConversionRate)
                .createdBy(createdBy)
                .build();

        return ResponseEntity.ok(unitService.filterUnits(filter));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Response> getUnitById(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.getUnitById(id));
    }
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> updateUnit(@PathVariable Long id, @RequestBody @Valid UnitDTO unitDTO) {
        return ResponseEntity.ok(unitService.updateUnit(id, unitDTO));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Response> deleteUnit(@PathVariable Long id) {
        return ResponseEntity.ok(unitService.deleteUnit(id));
    }


}

