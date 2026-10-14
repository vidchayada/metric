package metric.backend.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import metric.backend.entity.Category;
import metric.backend.entity.Order;
import metric.backend.entity.OrderItem;
import metric.backend.entity.Product;
import metric.backend.repository.CategoryRepository;
import metric.backend.repository.OrderRepository;
import metric.backend.repository.ProductAttributeRepository;
import metric.backend.repository.ProductRepository;
import metric.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final String CANCELLED = "cancelled";

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductAttributeRepository productAttributeRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardSummary() {
        Map<String, Object> data = new HashMap<>();

        List<Product> allProducts = productRepository.findAll();
        List<Category> allCategories = categoryRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();

        BigDecimal totalSales = calculateTotalSales(allOrders);
        data.put("totalMonth", totalSales);
        data.put("totalWeek", 0);
        data.put("totalSales", totalSales);
        data.put("totalRevenue", totalSales);
        data.put("totalOrders", (long) allOrders.size());
        data.put("totalUsers", userRepository.count());
        data.put("totalProduct", allProducts.size());
        data.put("totalProducts", allProducts.size());
        data.put("totalCategory", allCategories.size());
        data.put("totalCategories", allCategories.size());

        data.put("categories", buildCategoryList(allCategories, allProducts));

        List<Order> sortedOrders = sortOrdersByRecency(allOrders);
        List<Map<String, Object>> recentOrdersList = buildRecentOrdersList(sortedOrders);
        data.put("recentOrders", recentOrdersList);
        data.put("orders", recentOrdersList);

        Map<String, Integer> productSoldMap = calculateProductSoldMap(allOrders);
        data.put("itemSold", buildItemSoldList(productSoldMap));
        data.put("popularItems", buildPopularItems(productSoldMap));

        data.put("outOfStock", findOutOfStockProducts(allProducts));

        return ResponseEntity.ok(data);
    }

    private BigDecimal calculateTotalSales(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getTotalAmount() != null
                        && !CANCELLED.equalsIgnoreCase(String.valueOf(order.getStatus())))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<Map<String, Object>> buildCategoryList(List<Category> categories, List<Product> products) {
        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> catMap = new HashMap<>();
            catMap.put("id", cat.getCategoryId());
            catMap.put("name", cat.getCategoryName());

            List<String> productNames = products.stream()
                    .filter(p -> p.getCategory() != null && cat.getCategoryId().equals(p.getCategory().getCategoryId()))
                    .map(Product::getProductName)
                    .toList();

            catMap.put("products", productNames);
            categoryList.add(catMap);
        }
        return categoryList;
    }

    private List<Order> sortOrdersByRecency(List<Order> orders) {
        return orders.stream()
                .sorted((a, b) -> {
                    if (b.getCreatedAt() != null && a.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return Long.compare(b.getOrderId() != null ? b.getOrderId() : 0L,
                            a.getOrderId() != null ? a.getOrderId() : 0L);
                })
                .toList();
    }

    private List<Map<String, Object>> buildRecentOrdersList(List<Order> sortedOrders) {
        List<Map<String, Object>> recentOrdersList = new ArrayList<>();
        int limit = Math.min(sortedOrders.size(), 5);
        for (int i = 0; i < limit; i++) {
            Order ord = sortedOrders.get(i);
            Map<String, Object> ordMap = new HashMap<>();
            ordMap.put("number", resolveOrderNumber(ord, i));
            ordMap.put("date", resolveOrderDate(ord));
            ordMap.put("customer", resolveCustomerName(ord));
            recentOrdersList.add(ordMap);
        }
        return recentOrdersList;
    }

    private String resolveOrderNumber(Order ord, int fallbackIndex) {
        String ordNum = ord.getOrderNumber();
        if (ordNum == null || ordNum.isEmpty()) {
            return ord.getOrderId() != null ? "#" + ord.getOrderId() : "#" + (fallbackIndex + 1);
        }
        return ordNum;
    }

    private String resolveOrderDate(Order ord) {
        if (ord.getCreatedAt() != null) {
            return ord.getCreatedAt().toString().substring(0, 10);
        }
        if (ord.getOrderDate() != null) {
            return ord.getOrderDate().toString().substring(0, 10);
        }
        return "-";
    }

    private String resolveCustomerName(Order ord) {
        if (ord.getUser() == null) {
            return "Guest";
        }
        if (ord.getUser().getUsername() != null && !ord.getUser().getUsername().isEmpty()) {
            return ord.getUser().getUsername();
        }
        if (ord.getUser().getEmail() != null && !ord.getUser().getEmail().isEmpty()) {
            return ord.getUser().getEmail();
        }
        if (ord.getUser().getFullName() != null && !ord.getUser().getFullName().isEmpty()) {
            return ord.getUser().getFullName();
        }
        return "Guest";
    }

    private Map<String, Integer> calculateProductSoldMap(List<Order> orders) {
        Map<String, Integer> productSoldMap = new HashMap<>();
        for (Order ord : orders) {
            if (CANCELLED.equalsIgnoreCase(String.valueOf(ord.getStatus())) || ord.getOrderItems() == null) {
                continue;
            }
            for (OrderItem item : ord.getOrderItems()) {
                if (item.getProduct() != null) {
                    String pName = item.getProduct().getProductName();
                    productSoldMap.put(pName, productSoldMap.getOrDefault(pName, 0) + item.getQuantity());
                }
            }
        }
        return productSoldMap;
    }

    private List<Map<String, Object>> buildItemSoldList(Map<String, Integer> productSoldMap) {
        List<Map<String, Object>> itemSoldList = new ArrayList<>();
        productSoldMap.forEach((name, qty) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("quantity", qty);
            itemSoldList.add(map);
        });
        return itemSoldList;
    }

    private List<Map<String, Object>> buildPopularItems(Map<String, Integer> productSoldMap) {
        return productSoldMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", entry.getKey());
                    m.put("sold", entry.getValue());
                    return m;
                })
                .toList();
    }

    private List<String> findOutOfStockProducts(List<Product> products) {
        List<String> outOfStockProducts = new ArrayList<>();
        for (Product p : products) {
            var attrs = productAttributeRepository.findByProduct_ProductId(p.getProductId());
            int totalStock = 0;
            if (attrs != null && !attrs.isEmpty()) {
                totalStock = attrs.stream()
                        .mapToInt(a -> a.getStockQty() != null ? a.getStockQty() : 0)
                        .sum();
            }
            if (totalStock == 0) {
                outOfStockProducts.add(p.getProductName());
            }
        }
        return outOfStockProducts;
    }
}