/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.CheckoutRequest;
import metric.backend.dto.OrderItemResponse;
import metric.backend.dto.OrderResponse;
import metric.backend.entity.CartItem;
import metric.backend.entity.Order;
import metric.backend.entity.OrderItem;
import metric.backend.entity.OrderStatus;
import metric.backend.entity.ProductAttribute;
import metric.backend.entity.ProductImage;
import metric.backend.entity.User;
import metric.backend.repository.CartItemRepository;
import metric.backend.repository.OrderItemRepository;
import metric.backend.repository.OrderRepository;
import metric.backend.repository.ProductAttributeRepository;
import metric.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<CartItem> cartItems = cartItemRepository.findByUser_UserId(userId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout: Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            ProductAttribute attribute = item.getAttribute();
            if (attribute.getStockQty() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + item.getProduct().getProductName());
            }
            BigDecimal itemSubtotal = item.getProduct().getBasePrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemSubtotal);
        }

        String orderNumber = "#ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .totalAmount(totalAmount)
                .status(OrderStatus.pending)
                .orderDate(OffsetDateTime.now())
                .build();

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            ProductAttribute attribute = item.getAttribute();
            attribute.setStockQty(attribute.getStockQty() - item.getQuantity());
            productAttributeRepository.save(attribute);

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(item.getProduct())
                    .attribute(attribute)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getProduct().getBasePrice())
                    .build();

            orderItems.add(orderItemRepository.save(orderItem));
        }

        cartItemRepository.deleteByUser_UserId(userId);

        return mapToOrderResponse(savedOrder, orderItems);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUser_UserIdOrderByOrderDateDesc(userId);
        return orders.stream().map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
            return mapToOrderResponse(order, items);
        }).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to view this order");
        }

        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
        return mapToOrderResponse(order, items);
    }

    private OrderResponse mapToOrderResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream().map(item -> {
            String primaryImageUrl = null;
            if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                primaryImageUrl = item.getProduct().getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                        .map(ProductImage::getImageUrl)
                        .findFirst()
                        .orElse(item.getProduct().getImages().get(0).getImageUrl());
            }

            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            return OrderItemResponse.builder()
                    .orderItemId(item.getItemId())
                    .productId(item.getProduct().getProductId())
                    .productName(item.getProduct().getProductName())
                    .imageUrl(primaryImageUrl)
                    .attributeId(item.getAttribute() != null ? item.getAttribute().getAttributeId() : null)
                    .size(item.getAttribute() != null ? item.getAttribute().getSize() : null)
                    .color(item.getAttribute() != null ? item.getAttribute().getColor() : null)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .subtotal(subtotal)
                    .build();
        }).toList();

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUser() != null ? order.getUser().getUserId() : null)
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .orderDate(order.getOrderDate())
                .items(itemResponses)
                .build();
    }
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrdersForAdmin() {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        return orders.stream().map(order -> {
            List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
            return mapToOrderResponse(order, items);
        }).toList();
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, metric.backend.entity.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrder_OrderId(updatedOrder.getOrderId());
        return mapToOrderResponse(updatedOrder, items);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getFilteredOrdersForAdmin(String search, Integer month, String status) {
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();

        return orders.stream()
                .filter(order -> {
                    boolean matchesSearch = (search == null || search.trim().isEmpty()) ||
                            (order.getOrderNumber() != null && order.getOrderNumber().toLowerCase().contains(search.toLowerCase())) ||
                            (order.getUser() != null && order.getUser().getFullName() != null && order.getUser().getFullName().toLowerCase().contains(search.toLowerCase()));

                    boolean matchesMonth = (month == null) ||
                            (order.getOrderDate() != null && order.getOrderDate().getMonthValue() == month);

                    boolean matchesStatus = (status == null || status.trim().isEmpty()) ||
                            (order.getStatus() != null && order.getStatus().name().equalsIgnoreCase(status));

                    return matchesSearch && matchesMonth && matchesStatus;
                })
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrder_OrderId(order.getOrderId());
                    return mapToOrderResponse(order, items);
                })
                .toList();
    }

}