package com.phegondev.InventoryManagementSystem.user;

import com.phegondev.InventoryManagementSystem.enums.UserRole;
import com.phegondev.InventoryManagementSystem.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    long countByRole(UserRole role);

    java.util.List<User> findByBranchId(Long branchId);
    java.util.List<User> findByOrganizationId(Long organizationId);
    long countByBranchId(Long branchId);
}
