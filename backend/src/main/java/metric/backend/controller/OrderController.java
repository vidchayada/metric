/*
 * Copyright (c) 2026 Vidchayada
 * Licensed under the MIT License.
 */

package metric.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.CheckoutRequest;
import metric.backend.dto.OrderResponse;
import metric.backend.entity.User;
import metric.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(orderService.checkout(user.getUserId(), request));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getUserOrders(user.getUserId()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @AuthenticationPrincipal User user,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(user.getUserId(), orderId));
    }
}