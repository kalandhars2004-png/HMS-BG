package com.phegondev.InventoryManagementSystem.transaction;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.transaction.TransactionDTO;
import com.phegondev.InventoryManagementSystem.transaction.TransactionRequest;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.supplier.Supplier;
import com.phegondev.InventoryManagementSystem.transaction.Transaction;
import com.phegondev.InventoryManagementSystem.user.User;
import com.phegondev.InventoryManagementSystem.enums.TransactionStatus;
import com.phegondev.InventoryManagementSystem.enums.TransactionType;
import com.phegondev.InventoryManagementSystem.exceptions.InsufficientStockException;
import com.phegondev.InventoryManagementSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.supplier.SupplierRepository;
import com.phegondev.InventoryManagementSystem.transaction.TransactionRepository;
import com.phegondev.InventoryManagementSystem.transaction.TransactionService;
import com.phegondev.InventoryManagementSystem.stockmovement.StockMovementService;
import com.phegondev.InventoryManagementSystem.stockmovement.MovementType;
import com.phegondev.InventoryManagementSystem.alert.AlertService;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import com.phegondev.InventoryManagementSystem.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final ModelMapper modelMapper;
    private final SupplierRepository supplierRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;
    private final @Lazy AlertService alertService;



    @Override
    @Transactional
    public Response restockInventory(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();
        Long supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException("Supplier Id id Required");

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new NotFoundException("Product Not Found"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(()-> new NotFoundException("Supplier Not Found"));

        User user = userService.getCurrentLoggedInUser();
        var tenant = TenantContext.get();
        Long branchId = tenant != null && tenant.branchId() != null ? tenant.branchId() : (user.getBranchId() != null ? user.getBranchId() : 1L);
        Long orgId = tenant != null && tenant.organizationId() != null ? tenant.organizationId() : 1L;
        // branch cross-check §7
        if (tenant != null && !tenant.isSuperAdmin() && product.getBranchId() != null && !product.getBranchId().equals(branchId)) {
            throw new org.springframework.security.access.AccessDeniedException("Product branch mismatch");
        }

        //update the stock quantity and re-save
        product.setStockQuantity(product.getStockQuantity() + quantity);
        productRepository.save(product);

        //create a transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.PURCHASE)
                .status(TransactionStatus.COMPLETED)
                .branchId(branchId)
                .organizationId(orgId)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProducts(quantity)
                // Valued at purchase price, not the retail `price` — the old code
                // overstated the cost of every restock by the retail markup.
                .totalPrice((product.getPurchasePrice() != null ? product.getPurchasePrice() : product.getPrice())
                        .multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .build();

        transactionRepository.save(transaction);

        stockMovementService.record(
                product.getId(), product.getName(), product.getSku(), null,
                MovementType.PURCHASE, quantity, 0,
                product.getStockQuantity(), transaction.getId(), "Transaction", user.getName());

        try { alertService.checkProductStock(product); } catch (Exception e) { log.warn("Alert stock check failed", e); }
        try { alertService.notifyPurchaseCreated(transaction.getId(), transaction.getTotalPrice(), branchId); } catch (Exception e) { log.warn("Alert purchase notify failed", e); }

        return Response.builder()
                .status(200)
                .message("Transaction Made Successfully")
                .build();



    }

    @Override
    @Transactional
    public Response sell(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();
        Integer quantity = transactionRequest.getQuantity();


        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new NotFoundException("Product Not Found"));


        User user = userService.getCurrentLoggedInUser();

        // Check if sufficient stock is available
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock available. Available: " + product.getStockQuantity() + ", Requested: " + quantity);
        }

        //update the stock quantity and re-save
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        //create a transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.SALE)
                .status(TransactionStatus.COMPLETED)
                .branchId(TenantContext.get() != null && TenantContext.get().branchId() != null ? TenantContext.get().branchId() : (user.getBranchId() != null ? user.getBranchId() : 1L))
                .organizationId(TenantContext.get() != null && TenantContext.get().organizationId() != null ? TenantContext.get().organizationId() : 1L)
                .product(product)
                .user(user)
                .totalProducts(quantity)
                .totalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .description(transactionRequest.getDescription())
                .build();

        transactionRepository.save(transaction);

        stockMovementService.record(
                product.getId(), product.getName(), product.getSku(), null,
                MovementType.SALE, 0, quantity,
                product.getStockQuantity(), transaction.getId(), "Transaction", user.getName());

        try { alertService.checkProductStock(product); } catch (Exception e) { log.warn("Alert stock check failed", e); }
        try { alertService.notifySaleCreated(transaction.getId(), product.getName(), transaction.getTotalPrice(), transaction.getBranchId()); } catch (Exception e) { log.warn("Alert sale notify failed", e); }

        return Response.builder()
                .status(200)
                .message("Transaction Sold Successfully")
                .build();
    }

    @Override
    @Transactional
    public Response returnToSupplier(TransactionRequest transactionRequest) {

        Long productId = transactionRequest.getProductId();
        Long supplierId = transactionRequest.getSupplierId();
        Integer quantity = transactionRequest.getQuantity();

        if (supplierId == null) throw new NameValueRequiredException("Supplier Id id Required");

        Product product = productRepository.findById(productId)
                .orElseThrow(()-> new NotFoundException("Product Not Found"));

        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(()-> new NotFoundException("Supplier Not Found"));

        User user = userService.getCurrentLoggedInUser();

        // Check if sufficient stock is available
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Insufficient stock available. Available: " + product.getStockQuantity() + ", Requested: " + quantity);
        }

        //update the stock quantity and re-save
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        //create a transaction
        Transaction transaction = Transaction.builder()
                .transactionType(TransactionType.RETURN_TO_SUPPLIER)
                .status(TransactionStatus.PROCESSING)
                .branchId(TenantContext.get() != null && TenantContext.get().branchId() != null ? TenantContext.get().branchId() : (user.getBranchId() != null ? user.getBranchId() : 1L))
                .organizationId(TenantContext.get() != null && TenantContext.get().organizationId() != null ? TenantContext.get().organizationId() : 1L)
                .product(product)
                .user(user)
                .supplier(supplier)
                .totalProducts(quantity)
                .totalPrice(BigDecimal.ZERO)
                .description(transactionRequest.getDescription())
                .build();

        transactionRepository.save(transaction);

        stockMovementService.record(
                product.getId(), product.getName(), product.getSku(), null,
                MovementType.RETURN_TO_SUPPLIER, 0, quantity,
                product.getStockQuantity(), transaction.getId(), "Transaction", user.getName());

        return Response.builder()
                .status(200)
                .message("Transaction Returned Successfully Initialized")
                .build();
    }

    @Override
    public Response getAllTransactions(int page, int size, String searchText) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Transaction> transactionPage = transactionRepository.searchTransactions(searchText, pageable);

        List<TransactionDTO> transactionDTOS = modelMapper
                .map(transactionPage.getContent(), new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOS.forEach(transactionDTOItem -> {
            // user and supplier are still dropped to keep the payload small, but
            // `product` is retained: without it a purchase row cannot name the
            // medicine it moved, so nothing can report purchases per product.
            transactionDTOItem.setUser(null);
            transactionDTOItem.setSupplier(null);
        });


        return Response.builder()
                .status(200)
                .message("success")
                .transactions(transactionDTOS)
                .build();
    }

    @Override
    public Response getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Transaction Not Found"));

        TransactionDTO transactionDTO = modelMapper.map(transaction, TransactionDTO.class);

        transactionDTO.getUser().setTransactions(null); //removing the user trnasaction list

        return Response.builder()
                .status(200)
                .message("success")
                .transaction(transactionDTO)
                .build();

    }

    @Override
    public Response getAllTransactionByMonthAndYear(int month, int year) {

       List<Transaction> transactions = transactionRepository.findAllByMonthAndYear(month, year);

        List<TransactionDTO> transactionDTOS = modelMapper
                .map(transactions, new TypeToken<List<TransactionDTO>>() {}.getType());

        transactionDTOS.forEach(transactionDTOItem -> {
            // user and supplier are still dropped to keep the payload small, but
            // `product` is retained: without it a purchase row cannot name the
            // medicine it moved, so nothing can report purchases per product.
            transactionDTOItem.setUser(null);
            transactionDTOItem.setSupplier(null);
        });


        return Response.builder()
                .status(200)
                .message("success")
                .transactions(transactionDTOS)
                .build();
    }

@Override
    @Transactional
    public Response updateTransactionStatus(Long transactionId, TransactionStatus transactionStatus) {

        Transaction existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction Not Found"));

        TransactionStatus oldStatus = existingTransaction.getStatus();
        if (oldStatus == transactionStatus) {
            return Response.builder()
                    .status(200)
                    .message("Transaction already in status " + transactionStatus)
                    .build();
        }

        // Cancelling a COMPLETED transaction must reverse its stock effect —
        // a cancelled sale used to leave stock permanently deducted, and a
        // cancelled purchase permanently inflated.
        if (transactionStatus == TransactionStatus.CANCELED && oldStatus == TransactionStatus.COMPLETED) {
            Product product = existingTransaction.getProduct();
            if (product != null) {
                int qty = existingTransaction.getTotalProducts() != null ? existingTransaction.getTotalProducts() : 0;
                if (existingTransaction.getTransactionType() == TransactionType.PURCHASE) {
                    product.setStockQuantity(product.getStockQuantity() - qty);
                } else if (existingTransaction.getTransactionType() == TransactionType.SALE
                        || existingTransaction.getTransactionType() == TransactionType.RETURN_TO_SUPPLIER) {
                    product.setStockQuantity(product.getStockQuantity() + qty);
                }
                productRepository.save(product);
            }
        }
        // And the mirror case: un-cancelling re-applies the original effect so
        // stock is not silently left reversed.
        if (transactionStatus == TransactionStatus.COMPLETED && oldStatus == TransactionStatus.CANCELED) {
            Product product = existingTransaction.getProduct();
            if (product != null) {
                int qty = existingTransaction.getTotalProducts() != null ? existingTransaction.getTotalProducts() : 0;
                if (existingTransaction.getTransactionType() == TransactionType.PURCHASE) {
                    product.setStockQuantity(product.getStockQuantity() + qty);
                } else if (existingTransaction.getTransactionType() == TransactionType.SALE
                        || existingTransaction.getTransactionType() == TransactionType.RETURN_TO_SUPPLIER) {
                    product.setStockQuantity(product.getStockQuantity() - qty);
                }
                productRepository.save(product);
            }
        }

        existingTransaction.setStatus(transactionStatus);
        existingTransaction.setUpdatedAt(LocalDateTime.now());

        transactionRepository.save(existingTransaction);

        return Response.builder()
                .status(200)
                .message("Transaction Status Successfully Updated")
                .build();
    }
}
