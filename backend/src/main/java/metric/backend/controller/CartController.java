package metric.backend.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.AddToCartRequest;
import metric.backend.dto.CartResponse;
import metric.backend.dto.UpdateCartItemRequest;
import metric.backend.entity.User;
import metric.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping({"", "/items"})
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.ok(CartResponse.builder()
                    .items(Collections.emptyList())
                    .totalItems(0)
                    .grandTotal(BigDecimal.ZERO)
                    .build());
        }
        return ResponseEntity.ok(cartService.getCart(user.getUserId()));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Integer>> getCartCount(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of("count", 0));
        }

        try {
            CartResponse cart = cartService.getCart(user.getUserId());
            int count = 0;
            if (cart != null && cart.getItems() != null) {
                count = cart.getItems().stream()
                        .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 1)
                        .sum();
            }
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("count", 0));
        }
    }

    @PostMapping({"", "/items"})
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(user.getUserId(), request));
    }

    @RequestMapping(value = {"/{cartItemId}", "/items/{cartItemId}"}, method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(user.getUserId(), cartItemId, request));
    }

    @DeleteMapping({"/{cartItemId}", "/items/{cartItemId}"})
    public ResponseEntity<CartResponse> removeCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeCartItem(user.getUserId(), cartItemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        if (user != null) {
            cartService.clearCart(user.getUserId());
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> checkout(@AuthenticationPrincipal User user) {
        if (user != null) {
            cartService.clearCart(user.getUserId());
        }
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return ResponseEntity.ok(Map.of("orderNumber", orderNumber));
    }
}