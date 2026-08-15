package com.phegondev.InventoryManagementSystem.category;

import com.phegondev.InventoryManagementSystem.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    Optional<Category> findByCode(String code);
    boolean existsByName(String name);
    boolean existsByCode(String code);
    List<Category> findByNameContainingIgnoreCase(String name);
    List<Category> findByParentId(Long parentId);
    List<Category> findAllByOrderByDisplayOrderAsc();
}
