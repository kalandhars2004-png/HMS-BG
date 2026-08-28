package com.phegondev.InventoryManagementSystem.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import com.phegondev.InventoryManagementSystem.alert.AlertDTO;
import com.phegondev.InventoryManagementSystem.audit.AuditLogDTO;
import com.phegondev.InventoryManagementSystem.barcode.BarcodeScanDTO;
import com.phegondev.InventoryManagementSystem.batch.BatchDTO;
import com.phegondev.InventoryManagementSystem.brand.BrandDTO;
import com.phegondev.InventoryManagementSystem.category.CategoryDTO;
import com.phegondev.InventoryManagementSystem.enums.UserRole;
import com.phegondev.InventoryManagementSystem.equipment.EquipmentDTO;
import com.phegondev.InventoryManagementSystem.forecast.ForecastResult;
import com.phegondev.InventoryManagementSystem.forecast.ReorderPointDTO;
import com.phegondev.InventoryManagementSystem.invoice.InvoiceDTO;
import com.phegondev.InventoryManagementSystem.kitting.KitDTO;
import com.phegondev.InventoryManagementSystem.manufacturing.BillOfMaterialsDTO;
import com.phegondev.InventoryManagementSystem.manufacturing.ProductionOrderDTO;
import com.phegondev.InventoryManagementSystem.purchasereturn.PurchaseReturnDTO;
import com.phegondev.InventoryManagementSystem.purchaseorder.PurchaseOrderDTO;
import com.phegondev.InventoryManagementSystem.rma.ReturnRequestDTO;
import com.phegondev.InventoryManagementSystem.product.ProductDTO;
import com.phegondev.InventoryManagementSystem.rack.RackDTO;
import com.phegondev.InventoryManagementSystem.salesorder.SalesOrderDTO;
import com.phegondev.InventoryManagementSystem.stockmovement.StockMovement;
import com.phegondev.InventoryManagementSystem.stockadjustment.StockAdjustmentDTO;
import com.phegondev.InventoryManagementSystem.stockcount.StockCountDTO;
import com.phegondev.InventoryManagementSystem.stocktransfer.StockTransferDTO;
import com.phegondev.InventoryManagementSystem.supplier.SupplierDTO;
import com.phegondev.InventoryManagementSystem.transaction.TransactionDTO;
import com.phegondev.InventoryManagementSystem.unit.UnitDTO;
import com.phegondev.InventoryManagementSystem.user.UserDTO;
import com.phegondev.InventoryManagementSystem.variant.VariantDTO;
import com.phegondev.InventoryManagementSystem.warehouse.WarehouseDTO;
import com.phegondev.InventoryManagementSystem.branch.BranchDTO;
import com.phegondev.InventoryManagementSystem.customer.CustomerDTO;
import com.phegondev.InventoryManagementSystem.pos.POSSessionDTO;
import com.phegondev.InventoryManagementSystem.pos.POSTransactionDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response {
    //generic
    private int status;
    private String message;
    //for login
    private String token;
    private UserRole role;
    private String expirationTime;

    //for pagination
    private Integer totalPages;
    private Long totalElements;
    private Integer currentPage;
    private Integer pageSize;

    //data output optional
    private UserDTO user;
    private List<UserDTO> users;

    private SupplierDTO supplier;
    private List<SupplierDTO> suppliers;

    private CategoryDTO category;
    private List<CategoryDTO> categories;

    private ProductDTO product;
    private List<ProductDTO> products;

    private TransactionDTO transaction;
    private List<TransactionDTO> transactions;

    private BatchDTO batch;
    private List<BatchDTO> batches;

    private BrandDTO brand;
    private List<BrandDTO> brands;

    private UnitDTO unit;
    private List<UnitDTO> units;

    private VariantDTO variant;
    private List<VariantDTO> variants;

    private WarehouseDTO warehouse;
    private List<WarehouseDTO> warehouses;

    private BranchDTO branch;
    private List<BranchDTO> branches;

    private CustomerDTO customer;
    private List<CustomerDTO> customers;

    private RackDTO rack;
    private List<RackDTO> racks;

    private StockTransferDTO stockTransfer;
    private List<StockTransferDTO> stockTransfers;

    private EquipmentDTO equipment;
    private List<EquipmentDTO> equipments;

    private StockAdjustmentDTO stockAdjustment;
    private List<StockAdjustmentDTO> stockAdjustments;

    private PurchaseReturnDTO purchaseReturn;
    private List<PurchaseReturnDTO> purchaseReturns;

    private SalesOrderDTO salesOrder;
    private List<SalesOrderDTO> salesOrders;

    private StockCountDTO stockCount;
    private List<StockCountDTO> stockCounts;

    private InvoiceDTO invoice;
    private List<InvoiceDTO> invoices;

    private ReorderPointDTO reorderPoint;
    private List<ReorderPointDTO> reorderPoints;
    private List<ForecastResult> forecastResults;

    private List<AlertDTO> alerts;
    private AuditLogDTO auditLog;
    private List<AuditLogDTO> auditLogs;

    private ReturnRequestDTO returnRequest;
    private List<ReturnRequestDTO> returnRequests;

    private POSSessionDTO posSession;
    private List<POSSessionDTO> posSessions;
    private POSTransactionDTO posTransaction;
    private List<POSTransactionDTO> posTransactions;

    private BarcodeScanDTO barcodeScan;
    private List<BarcodeScanDTO> barcodeScans;

    private PurchaseOrderDTO purchaseOrder;
    private List<PurchaseOrderDTO> purchaseOrders;

    private KitDTO kit;
    private List<KitDTO> kits;

    private BillOfMaterialsDTO billOfMaterials;
    private List<BillOfMaterialsDTO> boms;

    private ProductionOrderDTO productionOrder;
    private List<ProductionOrderDTO> productionOrders;

    private List<StockMovement> stockMovements;

    // POS daily sales
    private BigDecimal totalSales;
    private BigDecimal totalRefunds;
    private Long transactionCount;

    private final LocalDateTime timestamp = LocalDateTime.now();






}
