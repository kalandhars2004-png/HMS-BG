package com.phegondev.InventoryManagementSystem.branch;

import java.util.List;

public interface BranchService {
    List<BranchDTO> getAll();
    List<BranchDTO> getActive();
    BranchDTO getById(Long id);
    BranchDTO create(BranchDTO dto);
    BranchDTO update(Long id, BranchDTO dto);
    void disable(Long id);
    void archive(Long id);
    BranchDTO assignManager(Long branchId, Long managerId);
    BranchDTO removeManager(Long branchId);
}
