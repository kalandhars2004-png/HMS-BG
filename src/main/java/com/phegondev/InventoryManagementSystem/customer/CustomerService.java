package com.phegondev.InventoryManagementSystem.customer;

import java.util.List;

public interface CustomerService {
    CustomerDTO create(CustomerDTO dto);
    CustomerDTO update(Long id, CustomerDTO dto);
    void delete(Long id);
    CustomerDTO getById(Long id);
    List<CustomerDTO> getAll();
    List<CustomerDTO> getByBranch(Long branchId);
}
