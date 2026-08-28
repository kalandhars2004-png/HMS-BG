package com.phegondev.InventoryManagementSystem.eod;

import com.phegondev.InventoryManagementSystem.alert.AlertRepository;
import com.phegondev.InventoryManagementSystem.batch.BatchRepository;
import com.phegondev.InventoryManagementSystem.common.Response;
import com.phegondev.InventoryManagementSystem.customer.CustomerRepository;
import com.phegondev.InventoryManagementSystem.inventory.InventoryMovementRepository;
import com.phegondev.InventoryManagementSystem.pos.POSTransaction;
import com.phegondev.InventoryManagementSystem.pos.POSTransactionRepository;
import com.phegondev.InventoryManagementSystem.product.Product;
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
import java.util.Objects;
import java.util.Set;
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
        // Date/status filtering happens in SQL now; previously this streamed every
        // POS transaction ever recorded through a Java filter.
        List<POSTransaction> todayTransactions = posTransactionRepository
                .findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc("COMPLETED", dayStart, dayEnd);

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
        int newCustomersToday = (int) customerRepository.countByCreatedAtBetween(dayStart, dayEnd);

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
        // Aggregate SQL counters; the old versions hydrated the whole product and
        // batch tables (twice each) just to produce four numbers.
        long lowStockCount = productRepository.countLowStock();
        long outOfStockCount = productRepository.countOutOfStock();
        LocalDateTime now = LocalDateTime.now();
        long nearExpiryCount = batchRepository.countByExpiryDateBetween(now, now.plusDays(90));
        long expiredCount = batchRepository.countByExpiryDateBefore(now);

        // ─── Inventory Value ───
        BigDecimal closingStockValue = productRepository.sumStockValue();

        // ─── Purchases ───
        BigDecimal totalPurchases = transactionRepository.sumTotalPriceByTypeAndCreatedAtBetween(
                com.phegondev.InventoryManagementSystem.enums.TransactionType.PURCHASE,
                dayStart, dayEnd);

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
        //
        // Products are fetched in one batch query; the loop used to issue two
        // findById calls per sold line item (2N queries per EOD run).
        Set<Long> soldProductIds = todayTransactions.stream()
                .map(POSTransaction::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Product> soldProducts = soldProductIds.isEmpty()
                ? Map.of()
                : productRepository.findAllById(soldProductIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

        BigDecimal costOfGoods = BigDecimal.ZERO;
        BigDecimal totalGst = BigDecimal.ZERO;
        for (POSTransaction t : todayTransactions) {
            if (t.getProductId() == null || t.getQuantity() == null) continue;
            Product product = soldProducts.get(t.getProductId());
            BigDecimal unitPrice = t.getUnitPrice() != null ? t.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal qty = BigDecimal.valueOf(t.getQuantity());
            costOfGoods = costOfGoods.add(
                    (product != null && product.getPurchasePrice() != null
                            ? product.getPurchasePrice() : unitPrice).multiply(qty));
            BigDecimal taxPct = product != null && product.getTaxPercentage() != null
                    ? product.getTaxPercentage() : BigDecimal.ZERO;
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