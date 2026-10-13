package metric.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.AddToCartRequest;
import metric.backend.dto.CartResponse;
import metric.backend.dto.UpdateCartItemRequest;
import metric.backend.entity.User;
import metric.backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(user.getUserId()));
    }

    @PostMapping
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(cartService.addToCart(user.getUserId(), request));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(user.getUserId(), cartItemId, request));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<CartResponse> removeCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeCartItem(user.getUserId(), cartItemId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user.getUserId());
        return ResponseEntity.noContent().build();
    }
}