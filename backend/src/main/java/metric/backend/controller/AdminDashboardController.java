/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import metric.backend.entity.Category;
import metric.backend.entity.Order;
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
        int limit = Math.min(allOrders.size(), 5);
        for (int i = 0; i < limit; i++) {
            Order ord = allOrders.get(i);
            Map<String, Object> ordMap = new HashMap<>();
            ordMap.put("number", ord.getOrderId() != null ? "#" + ord.getOrderId() : "#" + (i + 1));
            ordMap.put("date", ord.getCreatedAt() != null ? ord.getCreatedAt().toString().substring(0, 10) : "-");

            String customer = "—";
            if (ord.getUser() != null) {
                customer = ord.getUser().getUsername() != null ? ord.getUser().getUsername() : ord.getUser().getEmail();
            }
            ordMap.put("customer", customer);
            recentOrdersList.add(ordMap);
        }
        data.put("recentOrders", recentOrdersList);
        data.put("orders", recentOrdersList);
        data.put("itemSold", new ArrayList<>());

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