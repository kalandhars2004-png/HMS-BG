package com.phegondev.InventoryManagementSystem.customer;

import com.phegondev.InventoryManagementSystem.branch.BranchRepository;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BranchRepository branchRepository;

    private void enforce(Long branchId) {
        var t = TenantContext.get();
        if (t == null) throw new AccessDeniedException("No tenant");
        if (t.isSuperAdmin()) return;
        if (!branchId.equals(t.branchId())) throw new AccessDeniedException("Branch mismatch");
    }

    @Override
    @Transactional
    public CustomerDTO create(CustomerDTO dto) {
        var t = TenantContext.get();
        Long branchId = dto.getBranchId() != null ? dto.getBranchId() : (t != null && t.branchId() != null ? t.branchId() : 1L);
        Long orgId = t != null && t.organizationId() != null ? t.organizationId() : 1L;
        enforce(branchId);
        Customer c = Customer.builder()
                .branchId(branchId)
                .organizationId(orgId)
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .loyaltyPoints(dto.getLoyaltyPoints())
                .lifetimeSpend(dto.getLifetimeSpend())
                .purchaseCount(dto.getPurchaseCount())
                .build();
        c = customerRepository.save(c);
        return toDTO(c);
    }

    @Override
    @Transactional
    public CustomerDTO update(Long id, CustomerDTO dto) {
        Customer c = customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
        enforce(c.getBranchId());
        if (dto.getName() != null) c.setName(dto.getName());
        if (dto.getPhone() != null) c.setPhone(dto.getPhone());
        if (dto.getEmail() != null) c.setEmail(dto.getEmail());
        if (dto.getAddress() != null) c.setAddress(dto.getAddress());
        c = customerRepository.save(c);
        return toDTO(c);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Customer c = customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
        enforce(c.getBranchId());
        customerRepository.deleteById(id);
    }

    @Override
    public CustomerDTO getById(Long id) {
        Customer c = customerRepository.findById(id).orElseThrow(() -> new NotFoundException("Customer not found"));
        var t = TenantContext.get();
        if (t != null && !t.isSuperAdmin() && !c.getBranchId().equals(t.branchId())) throw new AccessDeniedException("Branch mismatch");
        return toDTO(c);
    }

    @Override
    public List<CustomerDTO> getAll() {
        var t = TenantContext.get();
        List<Customer> list;
        if (t != null && t.isSuperAdmin() && t.branchId() == null) {
            list = customerRepository.findAll();
        } else {
            Long bid = t != null && t.branchId() != null ? t.branchId() : 1L;
            list = customerRepository.findByBranchId(bid);
            if (list.isEmpty()) list = customerRepository.findAll();
        }
        return list.stream().map(this::toDTO).toList();
    }

    @Override
    public List<CustomerDTO> getByBranch(Long branchId) {
        enforce(branchId);
        return customerRepository.findByBranchId(branchId).stream().map(this::toDTO).toList();
    }

    private CustomerDTO toDTO(Customer c) {
        String branchName = null;
        if (c.getBranchId() != null) {
            branchName = branchRepository.findById(c.getBranchId()).map(b -> b.getName()).orElse(null);
        }
        return CustomerDTO.builder()
                .id(c.getId())
                .branchId(c.getBranchId())
                .organizationId(c.getOrganizationId())
                .branchName(branchName)
                .name(c.getName())
                .phone(c.getPhone())
                .email(c.getEmail())
                .address(c.getAddress())
                .loyaltyPoints(c.getLoyaltyPoints())
                .lifetimeSpend(c.getLifetimeSpend())
                .purchaseCount(c.getPurchaseCount())
                .lastPurchase(c.getLastPurchase())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
