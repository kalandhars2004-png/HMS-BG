package com.phegondev.InventoryManagementSystem.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.enums.TransactionStatus;
import com.phegondev.InventoryManagementSystem.enums.TransactionType;
import com.phegondev.InventoryManagementSystem.exceptions.InsufficientStockException;
import com.phegondev.InventoryManagementSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryManagementSystem.exceptions.NotFoundException;
import com.phegondev.InventoryManagementSystem.product.Product;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.supplier.Supplier;
import com.phegondev.InventoryManagementSystem.supplier.SupplierRepository;
import com.phegondev.InventoryManagementSystem.transaction.Transaction;
import com.phegondev.InventoryManagementSystem.transaction.TransactionRepository;
import com.phegondev.InventoryManagementSystem.transaction.TransactionRequest;
import com.phegondev.InventoryManagementSystem.user.User;
import com.phegondev.InventoryManagementSystem.user.UserService;

class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Product product;
    private Supplier supplier;
    private User user;
    private TransactionRequest transactionRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        product = new Product();
        product.setId(1L);
        product.setStockQuantity(10);
        product.setPrice(new BigDecimal("10.0"));

        supplier = new Supplier();
        supplier.setId(1L);

        user = new User();
        user.setId(1L);

        transactionRequest = new TransactionRequest();
        transactionRequest.setProductId(1L);
        transactionRequest.setQuantity(5);
        transactionRequest.setDescription("Test transaction");
    }

    @Test
    void sell_SuccessfulSale_ShouldReturnSuccessResponse() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userService.getCurrentLoggedInUser()).thenReturn(user);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // Act
        Response response = transactionService.sell(transactionRequest);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Transaction Sold Successfully", response.getMessage());
        verify(productRepository).save(product); // Verify stock was updated
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void sell_InsufficientStock_ShouldThrowInsufficientStockException() {
        // Arrange
        product.setStockQuantity(3); // Only 3 in stock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(userService.getCurrentLoggedInUser()).thenReturn(user);

        // Act & Assert
        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            transactionService.sell(transactionRequest);
        });

        assertEquals("Insufficient stock available. Available: 3, Requested: 5", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void returnToSupplier_SufficientStock_ShouldReturnSuccessResponse() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(userService.getCurrentLoggedInUser()).thenReturn(user);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        transactionRequest.setSupplierId(1L);

        // Act
        Response response = transactionService.returnToSupplier(transactionRequest);

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("Transaction Returned Successfully Initialized", response.getMessage());
        verify(productRepository).save(product); // Verify stock was updated
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void returnToSupplier_InsufficientStock_ShouldThrowInsufficientStockException() {
        // Arrange
        product.setStockQuantity(3); // Only 3 in stock
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(userService.getCurrentLoggedInUser()).thenReturn(user);
        transactionRequest.setSupplierId(1L);

        // Act & Assert
        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            transactionService.returnToSupplier(transactionRequest);
        });

        assertEquals("Insufficient stock available. Available: 3, Requested: 5", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void returnToSupplier_NullSupplierId_ShouldThrowNameValueRequiredException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        transactionRequest.setSupplierId(null); // Explicitly set to null

        // Act & Assert
        NameValueRequiredException exception = assertThrows(NameValueRequiredException.class, () -> {
            transactionService.returnToSupplier(transactionRequest);
        });

        assertEquals("Supplier Id id Required", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}