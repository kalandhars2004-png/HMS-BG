package com.phegondev.InventoryManagementSystem.dashboard;

import com.phegondev.InventoryManagementSystem.branch.BranchRepository;
import com.phegondev.InventoryManagementSystem.expense.ExpenseRepository;
import com.phegondev.InventoryManagementSystem.inventory.InventoryRepository;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.tenant.TenantContext;
import com.phegondev.InventoryManagementSystem.transaction.TransactionRepository;
import com.phegondev.InventoryManagementSystem.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final ExpenseRepository expenseRepository;
    private final InventoryRepository inventoryRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboard(
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String range // today, week, month, year, custom
    ) {
        var tenant = TenantContext.get();
        boolean isSuper = tenant != null && tenant.isSuperAdmin();
        Long effectiveBranch = branchId;
        if (!isSuper && tenant != null && tenant.branchId() != null) {
            effectiveBranch = tenant.branchId();
        } else if (isSuper && effectiveBranch == null) {
            // super admin All Branches
            effectiveBranch = null;
        }

        LocalDateTime from = resolveFrom(range);
        LocalDateTime to = LocalDateTime.now();

        Map<String, Object> data = new HashMap<>();

        // Branches
        long totalBranches = branchRepository.count();
        long activeBranches = branchRepository.findByStatus(com.phegondev.InventoryManagementSystem.branch.BranchStatus.ACTIVE).size();
        data.put("totalBranches", totalBranches);
        data.put("activeBranches", activeBranches);

        // Employees
        long totalEmployees = userRepository.count();
        data.put("totalEmployees", totalEmployees);

        // Medicines
        long totalMedicines = productRepository.count();
        data.put("totalMedicines", totalMedicines);

        // Stock
        long totalStock = 0;
        if (effectiveBranch != null) {
            Long s = inventoryRepository.sumQuantityByBranchId(effectiveBranch);
            totalStock = s != null ? s : 0;
            data.put("branchStock", totalStock);
            data.put("warehouseStock", totalStock); // for now same
        } else {
            Long s = inventoryRepository.sumQuantityByOrganizationId(1L);
            totalStock = s != null ? s : 0;
            data.put("warehouseStock", totalStock);
            data.put("branchStock", totalStock);
        }
        data.put("totalStock", totalStock);

        // Sales / Revenue
        // Use transactions table: filter by branch and date
        try {
            BigDecimal sales = BigDecimal.ZERO;
            long count = transactionRepository.count();
            data.put("totalTransactions", count);
            data.put("todaysSales", sales);
        } catch (Exception ignored) {
            data.put("todaysSales", BigDecimal.ZERO);
        }

        // Expenses
        try {
            BigDecimal expenses = BigDecimal.ZERO;
            if (effectiveBranch != null) {
                expenses = expenseRepository.sumByBranchAndDate(effectiveBranch, from, to);
            } else {
                expenses = expenseRepository.sumByOrgAndDate(1L, from, to);
            }
            data.put("expenses", expenses != null ? expenses : BigDecimal.ZERO);
        } catch (Exception ignored) {
            data.put("expenses", BigDecimal.ZERO);
        }

        // Branch performance
        List<Map<String, Object>> performance = new java.util.ArrayList<>();
        var branches = effectiveBranch != null ? branchRepository.findById(effectiveBranch).stream().toList() : branchRepository.findAll();
        for (var b : branches) {
            Map<String, Object> row = new HashMap<>();
            row.put("branchId", b.getId());
            row.put("branchName", b.getName());
            row.put("code", b.getCode());
            Long stock = inventoryRepository.sumQuantityByBranchId(b.getId());
            row.put("stockValue", stock != null ? stock : 0);
            row.put("sales", BigDecimal.ZERO); // TODO: aggregate sales per branch
            row.put("profit", BigDecimal.ZERO);
            performance.add(row);
        }
        data.put("branchPerformance", performance);
        data.put("effectiveBranchId", effectiveBranch);
        data.put("isSuperAdmin", isSuper);
        return ResponseEntity.ok(data);
    }

    private LocalDateTime resolveFrom(String range) {
        if (range == null) return LocalDateTime.now().minusMonths(1);
        return switch (range.toLowerCase()) {
            case "today" -> LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            case "yesterday" -> LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
            case "week", "this_week" -> LocalDateTime.now().minusDays(7);
            case "month", "this_month" -> LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            case "year", "this_year" -> LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
            default -> LocalDateTime.now().minusMonths(1);
        };
    }
}
