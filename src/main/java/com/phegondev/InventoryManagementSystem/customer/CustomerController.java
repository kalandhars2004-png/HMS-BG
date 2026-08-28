package com.phegondev.InventoryManagementSystem.customer;

import com.phegondev.InventoryManagementSystem.common.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/all")
    public ResponseEntity<Response> getAll() {
        List<CustomerDTO> list = customerService.getAll();
        return ResponseEntity.ok(Response.builder().status(200).message("success").customers(list).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getById(@PathVariable Long id) {
        CustomerDTO dto = customerService.getById(id);
        return ResponseEntity.ok(Response.builder().status(200).message("success").customer(dto).build());
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN','BRANCH_MANAGER','MANAGER','PHARMACIST','CASHIER')")
    public ResponseEntity<Response> create(@RequestBody CustomerDTO dto) {
        CustomerDTO created = customerService.create(dto);
        return ResponseEntity.ok(Response.builder().status(200).message("Customer created").customer(created).build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Response> update(@PathVariable Long id, @RequestBody CustomerDTO dto) {
        CustomerDTO updated = customerService.update(id, dto);
        return ResponseEntity.ok(Response.builder().status(200).message("Customer updated").customer(updated).build());
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN','BRANCH_MANAGER','MANAGER')")
    public ResponseEntity<Response> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.ok(Response.builder().status(200).message("Customer deleted").build());
    }

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<Response> getByBranch(@PathVariable Long branchId) {
        List<CustomerDTO> list = customerService.getByBranch(branchId);
        return ResponseEntity.ok(Response.builder().status(200).message("success").customers(list).build());
    }
}
