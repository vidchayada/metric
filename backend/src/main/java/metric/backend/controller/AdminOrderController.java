package metric.backend.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.OrderResponse;
import metric.backend.dto.PageResponse;
import metric.backend.dto.UpdateOrderStatusRequest;
import metric.backend.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getAllOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String status) {

        List<OrderResponse> filteredOrders = orderService.getFilteredOrdersForAdmin(search, month, status);

        PageResponse<OrderResponse> response = new PageResponse<>(filteredOrders, 1, 1);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody java.util.Map<String, String> payload) {

        String statusStr = payload.get("status");
        if (statusStr == null || statusStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
        metric.backend.entity.OrderStatus newStatus =
                metric.backend.entity.OrderStatus.valueOf(statusStr.toUpperCase());

        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }
}