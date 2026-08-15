package com.phegondev.InventoryManagementSystem.category;

import com.phegondev.InventoryManagementSystem.category.CategoryDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import java.util.List;

public interface CategoryService {
    Response createCategory(CategoryDTO categoryDTO);
    Response getAllCategories();
    Response getCategoryById(Long id);
    Response updateCategory(Long id, CategoryDTO categoryDTO);
    Response deleteCategory(Long id);
    Response bulkDelete(List<Long> ids);
    Response bulkStatusUpdate(List<Long> ids, boolean active);
    Response searchCategories(String query);
}
