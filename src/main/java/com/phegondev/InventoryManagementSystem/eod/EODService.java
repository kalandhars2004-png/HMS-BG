package com.phegondev.InventoryManagementSystem.eod;

import com.phegondev.InventoryManagementSystem.alert.AlertRepository;
import com.phegondev.InventoryManagementSystem.batch.BatchRepository;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.customer.CustomerRepository;
import com.phegondev.InventoryManagementSystem.inventory.InventoryMovementRepository;
import com.phegondev.InventoryManagementSystem.pos.POSTransaction;
import com.phegondev.InventoryManagementSystem.pos.POSTransactionRepository;
import com.phegondev.InventoryManagementSystem.product.ProductRepository;
import com.phegondev.InventoryManagementSystem.transaction.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EODService {

    private final DailyBusinessSummaryRepository summaryRepository;
    private final POSTransactionRepository posTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final BatchRepository batchRepository;
    private final AlertRepository alertRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Transactional
    public Response generateReport() {
        return generateReportForDate(LocalDate.now());
    }

    @Transactional
    public Response generateReportForDate(LocalDate date) {
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        // ─── Sales Data ───
        List<POSTransaction> todayTransactions = posTransactionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .filter(t -> t.getCreatedAt() != null
                        && !t.getCreatedAt().isBefore(dayStart)
                        && !t.getCreatedAt().isAfter(dayEnd)
                        && "COMPLETED".equals(t.getStatus()))
                .toList();

        BigDecimal totalSales = todayTransactions.stream()
                .map(t -> t.getTotalPrice() != null ? t.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalBills = todayTransactions.size();
        int totalItemsSold = todayTransactions.stream()
                .mapToInt(t -> t.getQuantity() != null ? t.getQuantity() : 0)
                .sum();

        // ─── Payment Breakdown ───
        Map<String, BigDecimal> paymentMap = todayTransactions.stream()
                .filter(t -> t.getPaymentMethod() != null)
                .collect(Collectors.groupingBy(
                        POSTransaction::getPaymentMethod,
                        Collectors.mapping(
                                t -> t.getTotalPrice() != null ? t.getTotalPrice() : BigDecimal.ZERO,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        BigDecimal cashSales = paymentMap.getOrDefault("CASH", BigDecimal.ZERO);
        BigDecimal upiSales = paymentMap.getOrDefault("UPI", BigDecimal.ZERO);
        BigDecimal cardSales = paymentMap.getOrDefault("CARD", BigDecimal.ZERO);
        BigDecimal walletSales = paymentMap.getOrDefault("WALLET", BigDecimal.ZERO);

        // ─── Customers ───
        long totalCustomers = customerRepository.count();
        long customersToday = todayTransactions.stream()
                .filter(t -> t.getCustomerName() != null && !"Walk-in Customer".equals(t.getCustomerName()))
                .map(POSTransaction::getCustomerName)
                .distinct()
                .count();
        int newCustomersToday = (int) customerRepository.findAll().stream()
                .filter(c -> c.getCreatedAt() != null
                        && !c.getCreatedAt().toLocalDate().isBefore(date)
                        && !c.getCreatedAt().toLocalDate().isAfter(date))
                .count();

        // ─── Bill Value Stats ───
        BigDecimal highestBill = BigDecimal.ZERO;
        BigDecimal lowestBill = BigDecimal.ZERO;
        if (!todayTransactions.isEmpty()) {
            highestBill = todayTransactions.stream()
                    .map(t -> t.getTotalPrice() != null ? t.getTotalPrice() : BigDecimal.ZERO)
                    .max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
            lowestBill = todayTransactions.stream()
                    .map(t -> t.getTotalPrice() != null ? t.getTotalPrice() : BigDecimal.ZERO)
                    .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        }
        BigDecimal averageBillValue = totalBills > 0 ? totalSales.divide(BigDecimal.valueOf(totalBills), BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO;

        // ─── Inventory ───
        long lowStockCount = productRepository.findAll().stream()
                .filter(p -> p.getLowStockQuantity() != null && p.getStockQuantity() <= p.getLowStockQuantity() && p.getStockQuantity() > 0)
                .count();
        long outOfStockCount = productRepository.findAll().stream()
                .filter(p -> p.getStockQuantity() == null || p.getStockQuantity() <= 0)
                .count();
        long nearExpiryCount = batchRepository.findAll().stream()
                .filter(b -> b.getExpiryDate() != null
                        && b.getExpiryDate().isAfter(LocalDateTime.now())
                        && b.getExpiryDate().isBefore(LocalDateTime.now().plusDays(90)))
                .count();
        long expiredCount = batchRepository.findAll().stream()
                .filter(b -> b.getExpiryDate() != null && b.getExpiryDate().isBefore(LocalDateTime.now()))
                .count();

        // ─── Inventory Value ───
        BigDecimal closingStockValue = productRepository.findAll().stream()
                .map(p -> {
                    BigDecimal price = p.getPurchasePrice() != null ? p.getPurchasePrice() : (p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO);
                    return price.multiply(BigDecimal.valueOf(p.getStockQuantity() != null ? p.getStockQuantity() : 0));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ─── Purchases ───
        BigDecimal totalPurchases = transactionRepository.findAll().stream()
                .filter(t -> t.getCreatedAt() != null
                        && !t.getCreatedAt().isBefore(dayStart)
                        && !t.getCreatedAt().isAfter(dayEnd)
                        && "PURCHASE".equals(t.getTransactionType().name()))
                .map(t -> t.getTotalPrice() != null ? t.getTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // ─── Top Medicine ───
        String topMedicine = todayTransactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getProductId() != null ? t.getProductId() : 0L,
                        Collectors.summingInt(t -> t.getQuantity() != null ? t.getQuantity() : 0)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> productRepository.findById(e.getKey())
                        .map(p -> p.getName() + " (" + e.getValue() + " sold)")
                        .orElse("N/A"))
                .orElse("N/A");

        // ─── Cost of Goods / Real Profit / Real GST ───
        // Previously these were invented constants (25% gross, 18% net, 5% GST of
        // revenue) persisted as business records. Now they come from actual data:
        // COGS from purchase price, GST from each product's tax percentage.
        BigDecimal costOfGoods = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        for (POSTransaction t : todayTransactions) {
            if (t.getProductId() == null || t.getQuantity() == null) continue;
            BigDecimal unitPrice = t.getUnitPrice() != null ? t.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal qty = BigDecimal.valueOf(t.getQuantity());
            costOfGoods = costOfGoods.add(productRepository.findById(t.getProductId())
                    .map(p -> p.getPurchasePrice() != null ? p.getPurchasePrice() : unitPrice)
                    .orElse(unitPrice).multiply(qty));
            BigDecimal taxPct = productRepository.findById(t.getProductId())
                    .map(p -> p.getTaxPercentage() != null ? p.getTaxPercentage() : BigDecimal.ZERO)
                    .orElse(BigDecimal.ZERO);
            if (taxPct.compareTo(BigDecimal.ZERO) > 0) {
                totalGst = totalGst.add(unitPrice.multiply(qty)
                        .multiply(taxPct).divide(taxPct.add(BigDecimal.valueOf(100)), 2, BigDecimal.ROUND_HALF_UP));
            }
        }
        BigDecimal grossProfit = totalSales.subtract(costOfGoods).max(BigDecimal.ZERO);
        BigDecimal netProfit = grossProfit;

        // Opening stock = yesterday's closing value (real delta), not today's
        // closing value which makes any open/close difference zero by construction.
        BigDecimal openingStockValue = summaryRepository.findByReportDate(date.minusDays(1))
                .map(DailyBusinessSummary::getClosingStockValue)
                .orElse(closingStockValue);

        // ─── Build Report ───
        DailyBusinessSummary report = DailyBusinessSummary.builder()
                .reportDate(date)
                .totalSales(totalSales)
                .totalBills(totalBills)
                .totalItemsSold(totalItemsSold)
                .totalRevenue(totalSales)
                .grossProfit(grossProfit)
                .netProfit(netProfit)
                .cashSales(cashSales)
                .upiSales(upiSales)
                .cardSales(cardSales)
                .walletSales(walletSales)
                .totalDiscount(BigDecimal.ZERO)
                .totalGst(totalGst)
                .totalRefunds(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .totalPurchases(totalPurchases)
                .purchaseOrdersCount(0)
                .totalCustomers((int) totalCustomers)
                .newCustomers(newCustomersToday)
                .averageBillValue(averageBillValue)
                .highestBill(highestBill)
                .lowestBill(lowestBill)
                .loyaltyPointsEarned(0)
                .openingStockValue(openingStockValue)
                .closingStockValue(closingStockValue)
                .lowStockCount((int) lowStockCount)
                .outOfStockCount((int) outOfStockCount)
                .nearExpiryCount((int) nearExpiryCount)
                .expiredCount((int) expiredCount)
                .cashDrawerOpening(BigDecimal.ZERO)
                .cashDrawerClosing(cashSales)
                .expectedCash(cashSales)
                .bestEmployee("")
                .bestEmployeeSales(0)
                .topMedicine(topMedicine)
                .topCategory("")
                .topCustomer("")
                .generatedBy("SYSTEM")
                .build();

        summaryRepository.save(report);

        log.info("EOD report generated for {}: Revenue={}, Bills={}, Items={}",
                date, totalSales, totalBills, totalItemsSold);

        return Response.builder()
                .status(200)
                .message("EOD report generated for " + date)
                .build();
    }

    public Response getReport(LocalDate date) {
        return summaryRepository.findByReportDate(date)
                .map(r -> Response.builder().status(200).build())
                .orElse(Response.builder().status(404).message("No report for " + date).build());
    }
}