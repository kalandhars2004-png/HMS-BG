package com.phegondev.InventoryManagementSystem.category;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    private String generateCode(String name) {
        return name.toUpperCase()
                .replaceAll("[^A-Z0-9\\s]", "")
                .replaceAll("\\s+", "_")
                .replaceAll("_+", "_")
                .trim();
    }

    @Override
    public Response createCategory(CategoryDTO categoryDTO) {
        String trimmedName = categoryDTO.getName().trim();
        if (categoryRepository.existsByName(trimmedName)) {
            return Response.builder().status(400).message("Category name already exists").build();
        }
        String code = categoryDTO.getCode() != null ? categoryDTO.getCode().trim() : generateCode(trimmedName);
        if (categoryRepository.existsByCode(code)) {
            return Response.builder().status(400).message("Category code already exists").build();
        }

        Category category = Category.builder()
                .name(trimmedName)
                .code(code)
                .slug(categoryDTO.getSlug() != null ? categoryDTO.getSlug() : generateSlug(trimmedName))
                .description(categoryDTO.getDescription())
                .icon(categoryDTO.getIcon())
                .color(categoryDTO.getColor())
                .displayOrder(categoryDTO.getDisplayOrder())
                .parentId(categoryDTO.getParentId())
                .status(categoryDTO.getStatus() != null ? categoryDTO.getStatus() : true)
                .createdBy(categoryDTO.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .build();

        categoryRepository.save(category);
        return Response.builder().status(200).message("Category created successfully").build();
    }

    @Override
    public Response getAllCategories() {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"));
        List<CategoryDTO> categoryDTOS = modelMapper.map(categories, new TypeToken<List<CategoryDTO>>() {}.getType());
        return Response.builder().status(200).message("success").categories(categoryDTOS).build();
    }

    @Override
    public Response getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
        return Response.builder().status(200).message("success").category(categoryDTO).build();
    }

    @Override
    public Response updateCategory(Long id, CategoryDTO categoryDTO) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category Not Found"));

        if (categoryDTO.getName() != null) {
            String trimmed = categoryDTO.getName().trim();
            if (!trimmed.equals(existing.getName()) && categoryRepository.existsByName(trimmed)) {
                return Response.builder().status(400).message("Category name already exists").build();
            }
            existing.setName(trimmed);
            existing.setSlug(generateSlug(trimmed));
        }
        if (categoryDTO.getCode() != null) {
            String trimmed = categoryDTO.getCode().trim();
            if (!trimmed.equals(existing.getCode()) && categoryRepository.existsByCode(trimmed)) {
                return Response.builder().status(400).message("Category code already exists").build();
            }
            existing.setCode(trimmed);
        }
        if (categoryDTO.getDescription() != null) existing.setDescription(categoryDTO.getDescription());
        if (categoryDTO.getIcon() != null) existing.setIcon(categoryDTO.getIcon());
        if (categoryDTO.getColor() != null) existing.setColor(categoryDTO.getColor());
        if (categoryDTO.getDisplayOrder() != null) existing.setDisplayOrder(categoryDTO.getDisplayOrder());
        if (categoryDTO.getParentId() != null) existing.setParentId(categoryDTO.getParentId());
        if (categoryDTO.getStatus() != null) existing.setStatus(categoryDTO.getStatus());
        if (categoryDTO.getUpdatedBy() != null) existing.setUpdatedBy(categoryDTO.getUpdatedBy());
        existing.setUpdatedAt(LocalDateTime.now());

        categoryRepository.save(existing);
        return Response.builder().status(200).message("Category Successfully Updated").build();
    }

    @Override
    public Response deleteCategory(Long id) {
        categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Category Not Found"));
        categoryRepository.deleteById(id);
        return Response.builder().status(200).message("Category Successfully Deleted").build();
    }

    @Override
    public Response bulkDelete(List<Long> ids) {
        List<Category> toDelete = categoryRepository.findAllById(ids);
        categoryRepository.deleteAll(toDelete);
        return Response.builder().status(200).message(toDelete.size() + " categories deleted").build();
    }

    @Override
    public Response bulkStatusUpdate(List<Long> ids, boolean active) {
        List<Category> categories = categoryRepository.findAllById(ids);
        categories.forEach(c -> { c.setStatus(active); c.setUpdatedAt(LocalDateTime.now()); });
        categoryRepository.saveAll(categories);
        return Response.builder().status(200).message(categories.size() + " categories updated").build();
    }

    @Override
    public Response searchCategories(String query) {
        List<Category> results = categoryRepository.findByNameContainingIgnoreCase(query);
        List<CategoryDTO> dtos = modelMapper.map(results, new TypeToken<List<CategoryDTO>>() {}.getType());
        return Response.builder().status(200).message("success").categories(dtos).build();
    }
}
