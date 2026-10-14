package metric.backend.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import metric.backend.entity.Order;
import metric.backend.entity.OrderItem;
import metric.backend.entity.Product;
import metric.backend.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class AdminSalesController {

    private static final String CANCELLED = "cancelled";
    private static final int DEFAULT_MONTH = 3;
    private static final int DEFAULT_YEAR = 2026;

    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSalesData(
            @RequestParam(required = false) Integer dailyMonth,
            @RequestParam(required = false) Integer dailyYear,
            @RequestParam(required = false) Integer monthlyYear) {

        List<Order> validOrders = orderRepository.findAll().stream()
                .filter(o -> o.getTotalAmount() != null && !CANCELLED.equalsIgnoreCase(String.valueOf(o.getStatus())))
                .toList();

        int targetMonth = dailyMonth != null ? dailyMonth : DEFAULT_MONTH;
        int targetYear = dailyYear != null ? dailyYear : DEFAULT_YEAR;
        int tYear = monthlyYear != null ? monthlyYear : DEFAULT_YEAR;

        Map<String, Object> response = new HashMap<>();
        response.put("daily", buildDailySales(validOrders, targetMonth, targetYear));
        response.put("monthly", buildMonthlySales(validOrders, tYear));
        response.put("topProducts", buildTopProducts(validOrders));

        return ResponseEntity.ok(response);
    }

    private List<Map<String, Object>> buildDailySales(List<Order> orders, int targetMonth, int targetYear) {
        Map<String, BigDecimal> dailyMap = new LinkedHashMap<>();
        for (Order o : orders) {
            OffsetDateTime orderDate = o.getOrderDate();
            if (orderDate == null || orderDate.getMonthValue() != targetMonth || orderDate.getYear() != targetYear) {
                continue;
            }
            String dayKey = String.format("%04d-%02d-%02d",
                    orderDate.getYear(), orderDate.getMonthValue(), orderDate.getDayOfMonth());
            dailyMap.merge(dayKey, o.getTotalAmount(), BigDecimal::add);
        }

        List<Map<String, Object>> dailySales = new ArrayList<>();
        dailyMap.forEach((date, sum) -> dailySales.add(toLabelSumMap(date, sum)));
        return dailySales;
    }

    private List<Map<String, Object>> buildMonthlySales(List<Order> orders, int targetYear) {
        Map<Integer, BigDecimal> monthlyMap = new TreeMap<>();
        for (Order o : orders) {
            OffsetDateTime orderDate = o.getOrderDate();
            if (orderDate == null || orderDate.getYear() != targetYear) {
                continue;
            }
            monthlyMap.merge(orderDate.getMonthValue(), o.getTotalAmount(), BigDecimal::add);
        }

        List<Map<String, Object>> monthlySales = new ArrayList<>();
        monthlyMap.forEach((month, sum) -> monthlySales.add(toLabelSumMap("Month " + month, sum)));
        return monthlySales;
    }

    private Map<String, Object> toLabelSumMap(String label, BigDecimal sum) {
        Map<String, Object> item = new HashMap<>();
        item.put("label", label);
        item.put("totalSales", sum);
        return item;
    }

    private List<Map<String, Object>> buildTopProducts(List<Order> orders) {
        Map<String, ProductSalesStat> productStatMap = new HashMap<>();
        for (Order o : orders) {
            if (o.getOrderDate() == null || o.getOrderItems() == null || o.getOrderItems().isEmpty()) {
                continue;
            }
            accumulateProductStats(o, productStatMap);
        }

        return productStatMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.totalOrders, a.totalOrders))
                .limit(5)
                .map(stat -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", stat.name);
                    m.put("imageUrl", stat.imageUrl);
                    m.put("totalOrders", stat.totalOrders);
                    return m;
                })
                .toList();
    }

    private void accumulateProductStats(Order order, Map<String, ProductSalesStat> productStatMap) {
        try {
            for (OrderItem item : order.getOrderItems()) {
                Product p = item.getProduct();
                if (p == null) {
                    continue;
                }
                String pName = p.getProductName();
                String imgUrl = (p.getImages() != null && !p.getImages().isEmpty())
                        ? p.getImages().get(0).getImageUrl()
                        : null;

                productStatMap.putIfAbsent(pName, new ProductSalesStat(pName, imgUrl, 0));
                productStatMap.get(pName).totalOrders += item.getQuantity();
            }
        } catch (Exception e) {
            //fix
        }
    }

    private static class ProductSalesStat {
        String name;
        String imageUrl;
        int totalOrders;

        public ProductSalesStat(String name, String imageUrl, int totalOrders) {
            this.name = name;
            this.imageUrl = imageUrl;
            this.totalOrders = totalOrders;
        }
    }
}