package metric.backend.controller;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import metric.backend.entity.Order;
import metric.backend.entity.OrderItem;
import metric.backend.entity.Product;
import metric.backend.repository.OrderItemRepository; // * เพิ่ม Repository สำหรับดึง OrderItem ถ้ามี
import metric.backend.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class AdminSalesController {

    private final OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSalesData(
            @RequestParam(required = false) Integer dailyMonth,
            @RequestParam(required = false) Integer dailyYear,
            @RequestParam(required = false) Integer monthlyYear) {

        Map<String, Object> response = new HashMap<>();
        List<Order> allOrders = orderRepository.findAll();

        List<Order> validOrders = allOrders.stream()
                .filter(o -> o.getTotalAmount() != null && !"cancelled".equalsIgnoreCase(String.valueOf(o.getStatus())))
                .toList();

        int targetMonth = dailyMonth != null ? dailyMonth : 3;
        int targetYear = dailyYear != null ? dailyYear : 2026;
        int tYear = monthlyYear != null ? monthlyYear : 2026;

        Map<String, BigDecimal> dailyMap = new LinkedHashMap<>();
        Map<Integer, BigDecimal> monthlyMap = new TreeMap<>();
        Map<String, ProductSalesStat> productStatMap = new HashMap<>();

        for (Order o : validOrders) {
            OffsetDateTime orderDate = o.getOrderDate();
            if (orderDate != null) {
                int oYear = orderDate.getYear();
                int oMonth = orderDate.getMonthValue();
                int oDay = orderDate.getDayOfMonth();

                if (oMonth == targetMonth && oYear == targetYear) {
                    String dayKey = String.format("%04d-%02d-%02d", oYear, oMonth, oDay);
                    dailyMap.put(dayKey, dailyMap.getOrDefault(dayKey, BigDecimal.ZERO).add(o.getTotalAmount()));
                }

                if (oYear == tYear) {
                    monthlyMap.put(oMonth, monthlyMap.getOrDefault(oMonth, BigDecimal.ZERO).add(o.getTotalAmount()));
                }

                try {
                    if (o.getOrderItems() != null && !o.getOrderItems().isEmpty()) {
                        for (OrderItem item : o.getOrderItems()) {
                            Product p = item.getProduct();
                            if (p != null) {
                                String pName = p.getProductName();
                                String imgUrl = null;
                                if (p.getImages() != null && !p.getImages().isEmpty()) {
                                    imgUrl = p.getImages().get(0).getImageUrl();
                                }

                                productStatMap.putIfAbsent(pName, new ProductSalesStat(pName, imgUrl, 0));
                                ProductSalesStat stat = productStatMap.get(pName);
                                stat.totalOrders += item.getQuantity();
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        List<Map<String, Object>> dailySales = new ArrayList<>();
        dailyMap.forEach((date, sum) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("label", date);
            item.put("totalSales", sum);
            dailySales.add(item);
        });

        List<Map<String, Object>> monthlySales = new ArrayList<>();
        monthlyMap.forEach((month, sum) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("label", "Month " + month);
            item.put("totalSales", sum);
            monthlySales.add(item);
        });

        List<Map<String, Object>> topProducts = productStatMap.values().stream()
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

        response.put("daily", dailySales);
        response.put("monthly", monthlySales);
        response.put("topProducts", topProducts);

        return ResponseEntity.ok(response);
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