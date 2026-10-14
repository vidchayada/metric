package metric.backend.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

        BigDecimal totalSales = allOrders.stream()
                .filter(order -> order.getTotalAmount() != null
                        && !"cancelled".equalsIgnoreCase(String.valueOf(order.getStatus())))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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

        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Category cat : allCategories) {
            Map<String, Object> catMap = new HashMap<>();
            catMap.put("id", cat.getCategoryId());
            catMap.put("name", cat.getCategoryName());

            List<String> productNames = allProducts.stream()
                    .filter(p -> p.getCategory() != null && cat.getCategoryId().equals(p.getCategory().getCategoryId()))
                    .map(Product::getProductName)
                    .toList();

            catMap.put("products", productNames);
            categoryList.add(catMap);
        }
        data.put("categories", categoryList);

        List<Map<String, Object>> recentOrdersList = new ArrayList<>();
        List<Order> sortedOrders = allOrders.stream()
                .sorted((a, b) -> {
                    if (b.getCreatedAt() != null && a.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return Long.compare(b.getOrderId() != null ? b.getOrderId() : 0L, a.getOrderId() != null ? a.getOrderId() : 0L);
                })
                .toList();

        int limit = Math.min(sortedOrders.size(), 5);
        for (int i = 0; i < limit; i++) {
            Order ord = sortedOrders.get(i);
            Map<String, Object> ordMap = new HashMap<>();

            String ordNum = ord.getOrderNumber();
            if (ordNum == null || ordNum.isEmpty()) {
                ordNum = ord.getOrderId() != null ? "#" + ord.getOrderId() : "#" + (i + 1);
            }
            ordMap.put("number", ordNum);

            String dateStr = "-";
            if (ord.getCreatedAt() != null) {
                dateStr = ord.getCreatedAt().toString().substring(0, 10);
            } else if (ord.getOrderDate() != null) {
                dateStr = ord.getOrderDate().toString().substring(0, 10);
            }
            ordMap.put("date", dateStr);

            String customer = "Guest";
            if (ord.getUser() != null) {
                if (ord.getUser().getUsername() != null && !ord.getUser().getUsername().isEmpty()) {
                    customer = ord.getUser().getUsername();
                } else if (ord.getUser().getEmail() != null && !ord.getUser().getEmail().isEmpty()) {
                    customer = ord.getUser().getEmail();
                } else if (ord.getUser().getFullName() != null && !ord.getUser().getFullName().isEmpty()) {
                    customer = ord.getUser().getFullName();
                }
            }
            ordMap.put("customer", customer);

            recentOrdersList.add(ordMap);
        }
        data.put("recentOrders", recentOrdersList);
        data.put("orders", recentOrdersList);

        Map<String, Integer> productSoldMap = new HashMap<>();
        int totalItemsSold = 0;

        for (Order ord : allOrders) {
            if (!"cancelled".equalsIgnoreCase(String.valueOf(ord.getStatus())) && ord.getOrderItems() != null) {
                for (OrderItem item : ord.getOrderItems()) {
                    totalItemsSold += item.getQuantity();
                    if (item.getProduct() != null) {
                        String pName = item.getProduct().getProductName();
                        productSoldMap.put(pName, productSoldMap.getOrDefault(pName, 0) + item.getQuantity());
                    }
                }
            }
        }

        List<Map<String, Object>> itemSoldList = new ArrayList<>();
        productSoldMap.forEach((name, qty) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("quantity", qty);
            itemSoldList.add(map);
        });
        data.put("itemSold", itemSoldList);

        List<Map<String, Object>> popularItems = productSoldMap.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(entry -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", entry.getKey());
                    m.put("sold", entry.getValue());
                    return m;
                })
                .toList();
        data.put("popularItems", popularItems);

        List<String> outOfStockProducts = new ArrayList<>();
        for (Product p : allProducts) {
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
        data.put("outOfStock", outOfStockProducts);

        return ResponseEntity.ok(data);
    }
}