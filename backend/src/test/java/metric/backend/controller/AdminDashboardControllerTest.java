package metric.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import metric.backend.entity.Category;
import metric.backend.entity.Order;
import metric.backend.entity.OrderItem;
import metric.backend.entity.Product;
import metric.backend.repository.CategoryRepository;
import metric.backend.repository.OrderRepository;
import metric.backend.repository.ProductAttributeRepository;
import metric.backend.repository.ProductRepository;
import metric.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class AdminDashboardControllerTest {

  @Mock OrderRepository orderRepository;
  @Mock ProductRepository productRepository;
  @Mock UserRepository userRepository;
  @Mock CategoryRepository categoryRepository;
  @Mock ProductAttributeRepository productAttributeRepository;

  @InjectMocks AdminDashboardController controller;

  @Test
  void getDashboardSummaryShouldHandleEmptyDatabase() {
    when(productRepository.findAll()).thenReturn(List.of());
    when(categoryRepository.findAll()).thenReturn(List.of());
    when(orderRepository.findAll()).thenReturn(List.of());
    when(userRepository.count()).thenReturn(0L);

    var response = controller.getDashboardSummary();

    assertEquals(HttpStatus.OK, response.getStatusCode());
    var data = response.getBody();
    assertNotNull(data);
    assertEquals(BigDecimal.ZERO, data.get("totalSales"));
    assertEquals(BigDecimal.ZERO, data.get("totalRevenue"));
    assertEquals(0L, data.get("totalOrders"));
    assertEquals(0L, data.get("totalUsers"));
    assertEquals(0, data.get("totalProducts"));
    assertEquals(0, data.get("totalCategories"));
    assertTrue(((List<?>) data.get("categories")).isEmpty());
    assertTrue(((List<?>) data.get("recentOrders")).isEmpty());
    assertTrue(((List<?>) data.get("itemSold")).isEmpty());
    assertTrue(((List<?>) data.get("popularItems")).isEmpty());
    assertTrue(((List<?>) data.get("outOfStock")).isEmpty());
  }

  @Test
  void getDashboardSummaryShouldExcludeCancelledSales() {
    Order paid = mock(Order.class);
    when(paid.getTotalAmount()).thenReturn(new BigDecimal("100.00"));
    when(paid.getStatus()).thenReturn(null);

    Order cancelled = mock(Order.class);
    when(cancelled.getTotalAmount()).thenReturn(new BigDecimal("50.00"));
    when(cancelled.getStatus()).thenReturn(null);
    // กรณีนี้ควรตั้ง status ของ cancelled ให้ตรงกับ enum/string
    // ที่ production code แปลงเป็น "cancelled"

    when(productRepository.findAll()).thenReturn(List.of());
    when(categoryRepository.findAll()).thenReturn(List.of());
    when(orderRepository.findAll()).thenReturn(List.of(paid, cancelled));
    when(userRepository.count()).thenReturn(2L);

    var response = controller.getDashboardSummary();

    assertNotNull(response.getBody());
    assertEquals(new BigDecimal("150.00"),
        response.getBody().get("totalSales"));
    assertEquals(2L, response.getBody().get("totalOrders"));
  }

  @Test
  void getDashboardSummaryShouldCountProductsByCategory() {
    Category category = mock(Category.class);
    when(category.getCategoryId()).thenReturn(1L);
    when(category.getCategoryName()).thenReturn("Shoes");

    Product product = mock(Product.class);
    when(product.getProductName()).thenReturn("Sneaker");
    when(product.getCategory()).thenReturn(category);
    when(product.getProductId()).thenReturn(10L);

    when(productRepository.findAll()).thenReturn(List.of(product));
    when(categoryRepository.findAll()).thenReturn(List.of(category));
    when(orderRepository.findAll()).thenReturn(List.of());
    when(userRepository.count()).thenReturn(1L);
    when(productAttributeRepository.findByProduct_ProductId(10L))
        .thenReturn(List.of());

    var response = controller.getDashboardSummary();
    var categories = (List<?>) response.getBody().get("categories");

    assertEquals(1, categories.size());
    assertEquals(List.of("Sneaker"),
        ((java.util.Map<?, ?>) categories.get(0)).get("products"));
    assertEquals(List.of("Sneaker"), response.getBody().get("outOfStock"));
  }

  @Test
  void getDashboardSummaryShouldIncludeSoldItems() {
    Product product = mock(Product.class);
    when(product.getProductName()).thenReturn("Sneaker");

    OrderItem item = mock(OrderItem.class);
    when(item.getQuantity()).thenReturn(3);
    when(item.getProduct()).thenReturn(product);

    Order order = mock(Order.class);
    when(order.getTotalAmount()).thenReturn(new BigDecimal("100"));
    when(order.getStatus()).thenReturn(null);
    when(order.getOrderItems()).thenReturn(List.of(item));

    when(productRepository.findAll()).thenReturn(List.of());
    when(categoryRepository.findAll()).thenReturn(List.of());
    when(orderRepository.findAll()).thenReturn(List.of(order));
    when(userRepository.count()).thenReturn(0L);

    var response = controller.getDashboardSummary();

    var sold = (List<?>) response.getBody().get("itemSold");
    assertEquals(1, sold.size());
    assertEquals("Sneaker",
        ((java.util.Map<?, ?>) sold.get(0)).get("name"));
    assertEquals(3,
        ((java.util.Map<?, ?>) sold.get(0)).get("quantity"));
  }
}