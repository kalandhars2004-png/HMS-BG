package com.phegondev.InventoryManagementSystem.branch;

import com.phegondev.InventoryManagementSystem.audit.AuditWriter;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.organization.OrganizationRepository;
import com.phegondev.InventoryManagementSystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuditWriter auditWriter;

    private static final Long DEFAULT_ORG_ID = 1L;

    @Override
    public List<BranchDTO> getAll() {
        var tenant = com.phegondev.InventoryManagementSystem.tenant.TenantContext.get();
        boolean isSuper = tenant != null && tenant.isSuperAdmin();
        if (isSuper) {
            return branchRepository.findAll().stream().map(this::toDTO).toList();
        }
        if (tenant != null && tenant.branchId() != null) {
            return branchRepository.findById(tenant.branchId()).stream().map(this::toDTO).toList();
        }
        return branchRepository.findByStatus(BranchStatus.ACTIVE).stream().map(this::toDTO).toList();
    }

    @Override
    public List<BranchDTO> getActive() {
        var tenant = com.phegondev.InventoryManagementSystem.tenant.TenantContext.get();
        boolean isSuper = tenant != null && tenant.isSuperAdmin();
        if (isSuper) {
            return branchRepository.findByStatus(BranchStatus.ACTIVE).stream().map(this::toDTO).toList();
        }
        if (tenant != null && tenant.branchId() != null) {
            return branchRepository.findById(tenant.branchId()).stream()
                    .filter(b -> b.getStatus() == BranchStatus.ACTIVE).map(this::toDTO).toList();
        }
        return branchRepository.findByStatus(BranchStatus.ACTIVE).stream().map(this::toDTO).toList();
    }

    @Override
    public BranchDTO getById(Long id) {
        Branch b = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + id));
        return toDTO(b);
    }

    @Override
    @Transactional
    @CacheEvict(value = "branches", allEntries = true)
    public BranchDTO create(BranchDTO dto) {
        if (branchRepository.findByCode(dto.getCode()).isPresent()) {
            throw new IllegalArgumentException("Branch code already exists: " + dto.getCode());
        }
        Long orgId = dto.getOrganizationId() != null ? dto.getOrganizationId() : DEFAULT_ORG_ID;
        // ensure organization exists
        organizationRepository.findById(orgId)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + orgId));

        Branch branch = Branch.builder()
                .organizationId(orgId)
                .code(dto.getCode())
                .name(dto.getName())
                .type(dto.getType() != null ? dto.getType() : BranchType.RETAIL)
                .status(dto.getStatus() != null ? dto.getStatus() : BranchStatus.ACTIVE)
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .postalCode(dto.getPostalCode())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .taxNumber(dto.getTaxNumber())
                .operatingHours(dto.getOperatingHours())
                .managerId(dto.getManagerId())
                .contactPerson(dto.getContactPerson())
                .build();
        branch = branchRepository.save(branch);
        auditWriter.write("Branch", String.valueOf(branch.getId()), "CREATE", "Created branch " + branch.getCode());
        log.info("Branch created: {} ({})", branch.getName(), branch.getCode());
        return toDTO(branch);
    }

    @Override
    @Transactional
    @CacheEvict(value = "branches", allEntries = true)
    public BranchDTO update(Long id, BranchDTO dto) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + id));

        // code is immutable after creation to avoid FK churn
        if (dto.getName() != null) branch.setName(dto.getName());
        if (dto.getType() != null) branch.setType(dto.getType());
        if (dto.getStatus() != null) branch.setStatus(dto.getStatus());
        if (dto.getAddress() != null) branch.setAddress(dto.getAddress());
        if (dto.getCity() != null) branch.setCity(dto.getCity());
        if (dto.getState() != null) branch.setState(dto.getState());
        if (dto.getCountry() != null) branch.setCountry(dto.getCountry());
        if (dto.getPostalCode() != null) branch.setPostalCode(dto.getPostalCode());
        if (dto.getPhone() != null) branch.setPhone(dto.getPhone());
        if (dto.getEmail() != null) branch.setEmail(dto.getEmail());
        if (dto.getTaxNumber() != null) branch.setTaxNumber(dto.getTaxNumber());
        if (dto.getOperatingHours() != null) branch.setOperatingHours(dto.getOperatingHours());
        if (dto.getContactPerson() != null) branch.setContactPerson(dto.getContactPerson());

        branch = branchRepository.save(branch);
        auditWriter.write("Branch", String.valueOf(branch.getId()), "UPDATE", "Updated branch " + branch.getCode());
        return toDTO(branch);
    }

    @Override
    @Transactional
    @CacheEvict(value = "branches", allEntries = true)
    public void disable(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + id));
        branch.setStatus(BranchStatus.DISABLED);
        branchRepository.save(branch);
        auditWriter.write("Branch", String.valueOf(id), "DISABLE", "Disabled branch " + branch.getCode());
    }

    @Override
    @Transactional
    @CacheEvict(value = "branches", allEntries = true)
    public void archive(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + id));
        branch.setStatus(BranchStatus.ARCHIVED);
        branchRepository.save(branch);
        auditWriter.write("Branch", String.valueOf(id), "ARCHIVE", "Archived branch " + branch.getCode());
    }

    @Override
    @Transactional
    @CacheEvict(value = "branches", allEntries = true)
    public BranchDTO assignManager(Long branchId, Long managerId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
        userRepository.findById(managerId)
                .orElseThrow(() -> new NotFoundException("User not found: " + managerId));
        branch.setManagerId(managerId);
        branch = branchRepository.save(branch);
        auditWriter.write("Branch", String.valueOf(branchId), "ASSIGN_MANAGER", "Assigned manager " + managerId + " to branch " + branch.getCode());
        return toDTO(branch);
    }

    @Override
    @Transactional
    @CacheEvict(value = "branches", allEntries = true)
    public BranchDTO removeManager(Long branchId) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new NotFoundException("Branch not found: " + branchId));
        branch.setManagerId(null);
        branch = branchRepository.save(branch);
        auditWriter.write("Branch", String.valueOf(branchId), "REMOVE_MANAGER", "Removed manager from branch " + branch.getCode());
        return toDTO(branch);
    }

    private BranchDTO toDTO(Branch b) {
        String managerName = null;
        if (b.getManagerId() != null) {
            managerName = userRepository.findById(b.getManagerId())
                    .map(u -> u.getName()).orElse(null);
        }
        return BranchDTO.builder()
                .id(b.getId())
                .organizationId(b.getOrganizationId())
                .code(b.getCode())
                .name(b.getName())
                .type(b.getType())
                .status(b.getStatus())
                .address(b.getAddress())
                .city(b.getCity())
                .state(b.getState())
                .country(b.getCountry())
                .postalCode(b.getPostalCode())
                .phone(b.getPhone())
                .email(b.getEmail())
                .taxNumber(b.getTaxNumber())
                .operatingHours(b.getOperatingHours())
                .managerId(b.getManagerId())
                .managerName(managerName)
                .contactPerson(b.getContactPerson())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
