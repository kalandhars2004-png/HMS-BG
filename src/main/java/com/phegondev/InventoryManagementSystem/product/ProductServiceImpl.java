package com.phegondev.InventoryManagementSystem.product;

import com.phegondev.InventoryManagementSystem.product.ProductDTO;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.category.Category;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.category.CategoryRepository;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.product.ProductService;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final CategoryRepository categoryRepository;

    private static final String IMAGE_DIRECTORY = System.getProperty("user.dir") + "/product-image/";

    private static final String IMAGE_DIRECTOR_FRONTEND = System.getProperty("user.dir") + "/public/products/";


    private void setProductFields(Product product, ProductDTO dto) {
        if (dto.getGenericName() != null) product.setGenericName(dto.getGenericName());
        if (dto.getBarcode() != null) product.setBarcode(dto.getBarcode());
        if (dto.getMrp() != null) product.setMrp(dto.getMrp());
        if (dto.getPurchasePrice() != null) product.setPurchasePrice(dto.getPurchasePrice());
        if (dto.getTaxPercentage() != null) product.setTaxPercentage(dto.getTaxPercentage());
        if (dto.getDiscountPercentage() != null) product.setDiscountPercentage(dto.getDiscountPercentage());
        if (dto.getLowStockQuantity() != null) product.setLowStockQuantity(dto.getLowStockQuantity());
        if (dto.getManufacturingDate() != null) product.setManufacturingDate(dto.getManufacturingDate());
        if (dto.getPrescriptionRequired() != null) product.setPrescriptionRequired(dto.getPrescriptionRequired());
        if (dto.getBrandId() != null) product.setBrandId(dto.getBrandId());
        if (dto.getUnitId() != null) product.setUnitId(dto.getUnitId());
        if (dto.getVariantId() != null) product.setVariantId(dto.getVariantId());
        if (dto.getWarehouseId() != null) product.setWarehouseId(dto.getWarehouseId());
        if (dto.getExpiryDate() != null) product.setExpiryDate(dto.getExpiryDate());
    }

    @Override
    public Response saveProduct(ProductDTO productDTO, MultipartFile imageFile) {

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(()-> new NotFoundException("Category Not Found"));

        // branch scoping §6
        var tenant = TenantContext.get();
        Long branchId = tenant != null ? tenant.branchId() : null;
        Long orgId = tenant != null ? tenant.organizationId() : 1L;
        // Super admin must explicitly select a branch via header; otherwise use 1
        if (branchId == null && tenant != null && tenant.isSuperAdmin()) {
            branchId = 1L;
        }
        if (branchId == null) branchId = 1L;

        //map out product dto to product entity
        Product productToSave = Product.builder()
                .name(productDTO.getName())
                .sku(productDTO.getSku())
                .price(productDTO.getPrice())
                .stockQuantity(productDTO.getStockQuantity())
                .description(productDTO.getDescription())
                .category(category)
                .branchId(branchId)
                .organizationId(orgId)
                .build();

        setProductFields(productToSave, productDTO);

        if (imageFile != null){
            String imagePath = saveImageToFrontendPublicFolder(imageFile);
            productToSave.setImageUrl(imagePath);
        }

        //save the product to our database
        productRepository.save(productToSave);
        return Response.builder()
                .status(200)
                .message("Product successfully saved")
                .build();
    }

    @Override
    public Response updateProduct(ProductDTO productDTO, MultipartFile imageFile) {

        Product existingProduct = productRepository.findById(productDTO.getProductId())
                .orElseThrow(()-> new NotFoundException("Product Not Found"));
        var t2 = TenantContext.get();
        if (t2 != null && !t2.isSuperAdmin() && existingProduct.getBranchId() != null && !existingProduct.getBranchId().equals(t2.branchId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied to product " + productDTO.getProductId());
        }

        //check if image is associated with the update request
        if (imageFile != null && !imageFile.isEmpty()){
            String imagePath = saveImageToFrontendPublicFolder(imageFile);
            existingProduct.setImageUrl(imagePath);
        }
        //Check if category is to be changed for the product
        if (productDTO.getCategoryId() != null && productDTO.getCategoryId() > 0){

            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(()-> new NotFoundException("Category Not Found"));
            existingProduct.setCategory(category);
        }

        //check and update fiedls

        if (productDTO.getName() !=null && !productDTO.getName().isBlank()){
            existingProduct.setName(productDTO.getName());
        }

        if (productDTO.getSku() !=null && !productDTO.getSku().isBlank()){
            existingProduct.setSku(productDTO.getSku());
        }

        if (productDTO.getDescription() !=null && !productDTO.getDescription().isBlank()){
            existingProduct.setDescription(productDTO.getDescription());
        }

        if (productDTO.getPrice() !=null && productDTO.getPrice().compareTo(BigDecimal.ZERO) >=0){
            existingProduct.setPrice(productDTO.getPrice());
        }

        if (productDTO.getStockQuantity() !=null && productDTO.getStockQuantity() >=0){
            existingProduct.setStockQuantity(productDTO.getStockQuantity());
        }

        setProductFields(existingProduct, productDTO);

        //Update the product
        productRepository.save(existingProduct);
        return Response.builder()
                .status(200)
                .message("Product successfully Updated")
                .build();

    }

    @Override
    public Response getAllProducts(Integer page, Integer size) {
        var tenant = TenantContext.get();
        boolean isSuper = tenant != null && tenant.isSuperAdmin();
        Long branchId = tenant != null ? tenant.branchId() : null;

        // Super admin with no branch header sees all (global)
        if (isSuper && branchId == null) {
            if (page == null || size == null) {
                List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
                return Response.builder()
                        .status(200)
                        .message("success")
                        .products(toProductDTOs(products))
                        .build();
            }
            Page<Product> productPage = productRepository.findAll(
                    PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 200),
                            Sort.by(Sort.Direction.DESC, "id")));
            return Response.builder()
                    .status(200)
                    .message("success")
                    .products(toProductDTOs(productPage.getContent()))
                    .totalPages(productPage.getTotalPages())
                    .totalElements(productPage.getTotalElements())
                    .currentPage(productPage.getNumber())
                    .pageSize(productPage.getSize())
                    .build();
        }

        // Branch-scoped
        Long effectiveBranch = branchId != null ? branchId : 1L;
        if (page == null || size == null) {
            List<Product> products = productRepository.findByBranchIdOrderByIdDesc(effectiveBranch);
            // Fallback: if no branch data yet (migrated), show legacy global
            if (products.isEmpty()) products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            return Response.builder()
                    .status(200)
                    .message("success")
                    .products(toProductDTOs(products))
                    .build();
        }
        // For paged, we still filter in-memory to keep impl simple for now; next phase will add Pageable branch query
        List<Product> all = productRepository.findByBranchIdOrderByIdDesc(effectiveBranch);
        if (all.isEmpty()) all = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        int from = Math.max(page, 0) * Math.min(Math.max(size, 1), 200);
        int to = Math.min(from + Math.min(Math.max(size, 1), 200), all.size());
        List<Product> content = from >= all.size() ? List.of() : all.subList(from, to);
        int totalPages = (int) Math.ceil((double) all.size() / Math.min(Math.max(size, 1), 200));
        return Response.builder()
                .status(200)
                .message("success")
                .products(toProductDTOs(content))
                .totalPages(totalPages)
                .totalElements((long) all.size())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    private List<ProductDTO> toProductDTOs(List<Product> products) {
        return products.stream().map(product -> {
            ProductDTO dto = modelMapper.map(product, ProductDTO.class);
            dto.setQuantity(product.getStockQuantity());
            if (product.getCategory() != null) {
                dto.setCategoryName(product.getCategory().getName());
            }
            return dto;
        }).toList();
    }

    @Override
    public Response getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Product Not Found"));
        var tenant = TenantContext.get();
        if (tenant != null && !tenant.isSuperAdmin() && product.getBranchId() != null && !product.getBranchId().equals(tenant.branchId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied to product " + id);
        }

        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        dto.setQuantity(product.getStockQuantity());
        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }

        return Response.builder()
                .status(200)
                .message("success")
                .product(dto)
                .build();
    }

    @Override
    public Response deleteProduct(Long id) {

        Product existing = productRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Product Not Found"));
        var t = TenantContext.get();
        if (t != null && !t.isSuperAdmin() && existing.getBranchId() != null && !existing.getBranchId().equals(t.branchId())) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied to product " + id);
        }

        productRepository.deleteById(id);

        return Response.builder()
                .status(200)
                .message("Product successfully deleted")
                .build();
    }

    private String saveImageToFrontendPublicFolder(MultipartFile imageFile){
        //validate image check
        if (!imageFile.getContentType().startsWith("image/")){
            throw new IllegalArgumentException("Only image files are allowed");
        }
        //create the directory to store images if it doesn't exist
        File directory = new File(IMAGE_DIRECTOR_FRONTEND);

        if (!directory.exists()){
            directory.mkdir();
            log.info("Directory was created");
        }
        //generate unique file name for the image
        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        //get the absolute path of the image
        String imagePath = IMAGE_DIRECTOR_FRONTEND + uniqueFileName;

        try {
            File desctinationFile = new File(imagePath);
            imageFile.transferTo(desctinationFile); //we are transfering(writing to this folder)

        }catch (Exception e){
            throw new IllegalArgumentException("Error occurend while saving image" + e.getMessage());
        }

        return "products/"+uniqueFileName;
    }

    private String saveImage(MultipartFile imageFile){
        //validate image check
        if (!imageFile.getContentType().startsWith("image/")){
            throw new IllegalArgumentException("Only image files are allowed");
        }
        //create the directory to store images if it doesn't exist
        File directory = new File(IMAGE_DIRECTORY);

        if (!directory.exists()){
            directory.mkdir();
            log.info("Directory was created");
        }
        //generate unique file name for the image
        String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        //get the absolute path of the image
        String imagePath = IMAGE_DIRECTORY + uniqueFileName;

        try {
            File desctinationFile = new File(imagePath);
            imageFile.transferTo(desctinationFile); //we are transfering(writing to this folder)

        }catch (Exception e){
            throw new IllegalArgumentException("Error occurend while saving image" + e.getMessage());
        }

        return imagePath;
    }











}
