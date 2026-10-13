package metric.backend.service;

import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import metric.backend.dto.AddToCartRequest;
import metric.backend.dto.CartItemResponse;
import metric.backend.dto.CartResponse;
import metric.backend.dto.UpdateCartItemRequest;
import metric.backend.entity.CartItem;
import metric.backend.entity.Product;
import metric.backend.entity.ProductAttribute;
import metric.backend.entity.ProductImage;
import metric.backend.entity.User;
import metric.backend.repository.CartItemRepository;
import metric.backend.repository.ProductAttributeRepository;
import metric.backend.repository.ProductRepository;
import metric.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUser_UserId(userId);
        List<CartItemResponse> itemResponses = items.stream().map(this::mapToItemResponse).toList();

        BigDecimal grandTotal = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return CartResponse.builder()
                .items(itemResponses)
                .totalItems(totalItems)
                .grandTotal(grandTotal)
                .build();
    }

    @Transactional
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        ProductAttribute attribute = productAttributeRepository.findById(request.getAttributeId())
                .orElseThrow(() -> new IllegalArgumentException("Product attribute not found"));

        if (attribute.getStockQty() < request.getQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        CartItem cartItem = cartItemRepository
                .findByUser_UserIdAndProduct_ProductIdAndAttribute_AttributeId(
                        userId, product.getProductId(), attribute.getAttributeId())
                .orElse(null);

        if (cartItem != null) {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (attribute.getStockQty() < newQuantity) {
                throw new IllegalArgumentException("Total quantity exceeds available stock");
            }
            cartItem.setQuantity(newQuantity);
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .attribute(attribute)
                    .quantity(request.getQuantity())
                    .build();
        }

        cartItemRepository.save(cartItem);
        return getCart(userId);
    }

    @Transactional
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!item.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to modify this cart item");
        }

        if (item.getAttribute().getStockQty() < request.getQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return getCart(userId);
    }

    @Transactional
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!item.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized to delete this cart item");
        }

        cartItemRepository.delete(item);
        return getCart(userId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUser_UserId(userId);
    }

    private CartItemResponse mapToItemResponse(CartItem item) {
        Product product = item.getProduct();
        ProductAttribute attribute = item.getAttribute();

        String primaryImageUrl = null;
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            primaryImageUrl = product.getImages().stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .map(ProductImage::getImageUrl)
                    .findFirst()
                    .orElse(product.getImages().get(0).getImageUrl());
        }

        BigDecimal unitPrice = product.getBasePrice();
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        return CartItemResponse.builder()
                .cartItemId(item.getCartItemId())
                .productId(product.getProductId())
                .productName(product.getProductName())
                .imageUrl(primaryImageUrl)
                .attributeId(attribute.getAttributeId())
                .size(attribute.getSize())
                .color(attribute.getColor())
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .build();
    }
}